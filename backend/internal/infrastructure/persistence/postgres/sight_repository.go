package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
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