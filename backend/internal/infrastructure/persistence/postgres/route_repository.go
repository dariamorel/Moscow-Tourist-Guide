package postgres

import (
	"context"
	"time"
	"fmt"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/value_object"
	"tour-guide/internal/domain/repository"
)

type routeRepo struct {
 	db *pgxpool.Pool
}

func NewRouteRepo(db *pgxpool.Pool) repository.RouteRepository {
 	return &routeRepo{db: db}
}

func (r *routeRepo) Create(ctx context.Context, route *entity.Route) error {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	duration, err := valueobject.ParseDuration(route.TotalTime)
	if err != nil {
		return fmt.Errorf("invalid total time format: %w", err)
	}

	q1 := `
	INSERT INTO tour_guide.routes (user_id, status, total_time, date, created_at, updated_at)
	VALUES ($1, $2, $3, $4, NOW(), NOW()) RETURNING id
	`
	err = tx.QueryRow(ctx, q1, route.UserID, route.Status, duration.String(), route.Date).Scan(&route.ID)
	if err != nil {
		return mapError(err)
	}

	for i, sightID := range route.PlannedOrder {
		status := "planned"
		q2 := `
		INSERT INTO tour_guide.route_items (route_id, sight_id, stop_order, status)
		VALUES ($1, $2, $3, $4)
		`
		if _, err := tx.Exec(ctx, q2, route.ID, sightID, i+1, status); err != nil {
			return mapError(err)
		}
	}

	return tx.Commit(ctx)
}

func (r *routeRepo) Update(ctx context.Context, route *entity.Route) error {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	duration, err := valueobject.ParseDuration(route.TotalTime)
	if err != nil {
		return fmt.Errorf("invalid total time format: %w", err)
	}

	q1 := `
	UPDATE tour_guide.routes 
	SET status = $2, total_time = $3, date = $4, updated_at = NOW()
	WHERE id = $1
	`
	res, err := tx.Exec(ctx, q1, route.ID, route.Status, duration.String(), route.Date)
	if err != nil {
		return mapError(err)
	}
	if res.RowsAffected() == 0 {
		return domain.ErrNotFound
	}

	if len(route.PlannedOrder) > 0 || len(route.Order) > 0 {
		_, _ = tx.Exec(ctx, `DELETE FROM tour_guide.route_items WHERE route_id = $1`, route.ID)

		visitedSet := make(map[int]bool)
		for _, id := range route.Order {
			visitedSet[id] = true
		}

		for i, sightID := range route.PlannedOrder {
			status := "planned"
			if visitedSet[sightID] {
				status = "visited"
			}
			q2 := `INSERT INTO tour_guide.route_items (route_id, sight_id, stop_order, status) VALUES ($1, $2, $3, $4)`
			if _, err := tx.Exec(ctx, q2, route.ID, sightID, i+1, status); err != nil {
				return mapError(err)
			}
		}
	}

	return tx.Commit(ctx)
}

func (r *routeRepo) GetByID(ctx context.Context, id int) (*entity.Route, error) {
	query := `
	SELECT r.id, r.user_id, r.status, EXTRACT(EPOCH FROM r.total_time), r.date, r.created_at, r.updated_at,
		COALESCE(array_agg(CASE WHEN ri.status = 'visited' THEN ri.sight_id END) FILTER (WHERE ri.status = 'visited'), '{}')::bigint[],
		COALESCE(array_agg(ri.sight_id ORDER BY ri.stop_order), '{}')::bigint[]
	FROM tour_guide.routes r
	LEFT JOIN tour_guide.route_items ri ON r.id = ri.route_id
	WHERE r.id = $1
	GROUP BY r.id
	`

	var route entity.Route
	var order, planned []int
	var totalSeconds float64

	err := r.db.QueryRow(ctx, query, id).Scan(
		&route.ID, &route.UserID, &route.Status, &totalSeconds,
		&route.Date, &route.CreatedAt, &route.UpdatedAt, &order, &planned,
	)
	if err != nil {
		return nil, mapError(err)
	}
	route.Order = order
	route.PlannedOrder = planned
	route.TotalTime = formatInterval(totalSeconds)
	return &route, nil
}

func (r *routeRepo) List(ctx context.Context, filter repository.RouteFilter) ([]*entity.Route, error) {
	query := `
	SELECT r.id, r.user_id, r.status, EXTRACT(EPOCH FROM r.total_time), r.date, r.created_at, r.updated_at,
		COALESCE(array_agg(CASE WHEN ri.status = 'visited' THEN ri.sight_id END) FILTER (WHERE ri.status = 'visited'), '{}')::bigint[],
		COALESCE(array_agg(ri.sight_id ORDER BY ri.stop_order), '{}')::bigint[]
	FROM tour_guide.routes r
	LEFT JOIN tour_guide.route_items ri ON r.id = ri.route_id
	WHERE r.user_id = $1 AND ($2::text IS NULL OR r.status = $2)
	GROUP BY r.id
	ORDER BY r.date DESC
	`

	rows, err := r.db.Query(ctx, query, filter.UserID, filter.Status)
	if err != nil {
		return nil, mapError(err)
	}
	defer rows.Close()

	var routes []*entity.Route
	for rows.Next() {
		var r entity.Route
		var order, planned []int
		var totalSeconds float64
		if err := rows.Scan(&r.ID, &r.UserID, &r.Status, &totalSeconds, &r.Date, &r.CreatedAt, &r.UpdatedAt, &order, &planned); err != nil {
			return nil, err
		}
		r.Order = order
		r.PlannedOrder = planned
		r.TotalTime = formatInterval(totalSeconds)
		routes = append(routes, &r)
	}
	return routes, rows.Err()
}

func (r *routeRepo) Delete(ctx context.Context, id int) error {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	_, _ = tx.Exec(ctx, `DELETE FROM tour_guide.route_items WHERE route_id = $1`, id)
	res, err := tx.Exec(ctx, `DELETE FROM tour_guide.routes WHERE id = $1`, id)
	if err != nil {
		return mapError(err)
	}
	if res.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return tx.Commit(ctx)
}

func formatInterval(seconds float64) string {
	duration := time.Duration(seconds * float64(time.Second))
	return valueobject.FormatDuration(duration)
}
