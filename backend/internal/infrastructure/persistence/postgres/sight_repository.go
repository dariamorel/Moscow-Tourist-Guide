package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
	"tour-guide/internal/domain/value_object"
)

type sightRepo struct {
 	db *pgxpool.Pool
}

func NewSightRepo(db *pgxpool.Pool) repository.SightRepository {
 	return &sightRepo{db: db}
}

func (r *sightRepo) GetByID(ctx context.Context, id int) (*entity.Sight, error) {
	query := `
	SELECT id, img, lat, lon, short_desc, full_desc, 
			COALESCE(timetable, ''), COALESCE(addr, ''), COALESCE(phone, ''), COALESCE(subway, ''),
			created_at, updated_at
	FROM tour_guide.sights
	WHERE id = $1
	`

	var s entity.Sight
	err := r.db.QueryRow(ctx, query, id).Scan(
		&s.ID, &s.Img, &s.Coordinates.Lat, &s.Coordinates.Lon,
		&s.ShortDesc, &s.Description,
		&s.Timetable, &s.Addr, &s.Phone, &s.Subway,
		&s.CreatedAt, &s.UpdatedAt,
	)
	if err != nil {
		return nil, mapError(err)
	}

	tags, err := r.loadTags(ctx, id)
	if err != nil {
		return nil, err
	}
	s.Tags = tags
	return &s, nil
}

func (r *sightRepo) List(ctx context.Context, filter repository.SightFilter) ([]*entity.Sight, error) {
	baseQuery := `
	SELECT DISTINCT s.id, s.img, s.lat, s.lon, s.short_desc, s.full_desc,
			COALESCE(s.timetable, ''), COALESCE(s.addr, ''), COALESCE(s.phone, ''), COALESCE(s.subway, ''),
			s.created_at, s.updated_at
	FROM tour_guide.sights s
	LEFT JOIN sight_to_tag stt ON s.id = stt.sight_id
	LEFT JOIN tags t ON stt.tag_id = t.id
	WHERE ($3::text IS NULL OR t.name = $3)
	ORDER BY s.id
	LIMIT $1 OFFSET $2
	`

	rows, err := r.db.Query(ctx, baseQuery, filter.Limit, filter.Offset, filter.Tag)
	if err != nil {
		return nil, mapError(err)
	}
	defer rows.Close()

	var sights []*entity.Sight
	for rows.Next() {
		var s entity.Sight
		if err := rows.Scan(
			&s.ID, &s.Img, &s.Coordinates.Lat, &s.Coordinates.Lon,
			&s.ShortDesc, &s.Description,
			&s.Timetable, &s.Addr, &s.Phone, &s.Subway,
			&s.CreatedAt, &s.UpdatedAt,
		); err != nil {
			return nil, err
		}

		s.Tags, _ = r.loadTags(ctx, s.ID)
		sights = append(sights, &s)
	}
	return sights, rows.Err()
}

func (r *sightRepo) loadTags(ctx context.Context, sightID int) ([]string, error) {
	query := `SELECT t.name FROM tour_guide.sight_to_tag stt JOIN tour_guide.tags t ON stt.tag_id = t.id WHERE stt.sight_id = $1`
	rows, err := r.db.Query(ctx, query, sightID)
	if err != nil {
		return nil, mapError(err)
	}
	defer rows.Close()

	var tags []string
	for rows.Next() {
		var tag string
		if err := rows.Scan(&tag); err != nil {
			return nil, err
		}
		tags = append(tags, tag)
	}
	return tags, rows.Err()
}

func (r *sightRepo) Create(ctx context.Context, sight *entity.Sight) error {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	q1 := `
	INSERT INTO tour_guide.sights (id, img, lat, lon, short_desc, full_desc, timetable, addr, phone, subway, created_at, updated_at)
	VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, NOW(), NOW())
	`
	_, err = tx.Exec(ctx, q1, sight.ID, sight.Img, sight.Coordinates.Lat, sight.Coordinates.Lon,
	sight.ShortDesc, sight.Description, sight.Timetable, sight.Addr, sight.Phone, sight.Subway)
	if err != nil {
		return mapError(err)
	}

	q2 := `SELECT id, lat, lon FROM tour_guide.sights WHERE id != $1`
	rows, err := tx.Query(ctx, q2, sight.ID)
	if err != nil {
		return err
	}
	defer rows.Close()

	var existingSights []struct {
		ID  int
		Lat float64
		Lon float64
	}
	for rows.Next() {
		var s struct{ ID int; Lat, Lon float64 }
		if err := rows.Scan(&s.ID, &s.Lat, &s.Lon); err != nil {
			return err
		}
		existingSights = append(existingSights, s)
	}

	q3 := `
	INSERT INTO tour_guide.time_between_sights (sight1_id, sight2_id, travel_time, distance_meters)
	VALUES ($1, $2, $3, $4)
	ON CONFLICT (sight1_id, sight2_id) DO NOTHING
	`
	newCoords := &valueobject.Coordinates{Lat: sight.Coordinates.Lat, Lon: sight.Coordinates.Lon}

	for _, existing := range existingSights {
		existingCoords := &valueobject.Coordinates{Lat: existing.Lat, Lon: existing.Lon}
		
		duration := newCoords.WalkingTimeTo(existingCoords)
		distanceMeters := int(newCoords.DistanceTo(existingCoords) * 1000)

		_, err = tx.Exec(ctx, q3, sight.ID, existing.ID, duration, distanceMeters)
		if err != nil {
			return err
		}

		_, err = tx.Exec(ctx, q3, existing.ID, sight.ID, duration, distanceMeters)
		if err != nil {
			return err
		}
	}

	return tx.Commit(ctx)
}

func (r *sightRepo) GetAllBasic(ctx context.Context) ([]struct{ ID int; Lat, Lon float64 }, error) {
	q := `SELECT id, lat, lon FROM tour_guide.sights`
	rows, err := r.db.Query(ctx, q)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []struct{ ID int; Lat, Lon float64 }
	for rows.Next() {
		var s struct{ ID int; Lat, Lon float64 }
		if err := rows.Scan(&s.ID, &s.Lat, &s.Lon); err != nil {
			return nil, err
		}
		result = append(result, s)
	}
	return result, rows.Err()
}
