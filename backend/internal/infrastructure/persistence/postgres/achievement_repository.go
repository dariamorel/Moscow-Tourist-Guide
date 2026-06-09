package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type achievementRepo struct {
 	db *pgxpool.Pool
}

func NewAchievementRepo(db *pgxpool.Pool) repository.AchievementRepository {
 	return &achievementRepo{db: db}
}

func (r *achievementRepo) ListAll(ctx context.Context, userID int) ([]*entity.Achievement, error) {
	var query string
	var args []any

	if userID > 0 {
		query = `
		SELECT a.id, a.type, a.name, COALESCE(a.description,''), 
				COALESCE(a.icon_url,''), a.required_count, COALESCE(a.tag,''),
				COALESCE(ua.progress, 0) as progress
		FROM tour_guide.achievements a
		LEFT JOIN tour_guide.user_achievements ua 
			ON ua.achievement_id = a.id AND ua.user_id = $1
		ORDER BY a.id
		`
		args = []any{userID}
	} else {
		query = `
		SELECT id, type, name, COALESCE(description,''), 
				COALESCE(icon_url,''), required_count, COALESCE(tag,''),
				0 as progress
		FROM tour_guide.achievements
		ORDER BY id
		`
	}

	rows, err := r.db.Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []*entity.Achievement
	for rows.Next() {
		var a entity.Achievement
		var typeStr string
		if err := rows.Scan(&a.ID, &typeStr, &a.Name, &a.Description, &a.IconURL, &a.RequiredCount, &a.Tag, &a.Progress); err != nil {
			return nil, err
		}
		a.Type = entity.AchievementType(typeStr)
		list = append(list, &a)
	}
	return list, rows.Err()
}

func (r *achievementRepo) GetCompletedByUserID(ctx context.Context, userID int) ([]*entity.Achievement, error) {
	q := `
	SELECT a.id, a.type, a.name, COALESCE(a.description,''), 
			COALESCE(a.icon_url,''), a.required_count, COALESCE(a.tag,''),
			COALESCE(ua.progress, 0) as progress
	FROM tour_guide.user_achievements ua
	JOIN tour_guide.achievements a ON ua.achievement_id = a.id
	WHERE ua.user_id = $1 AND ua.unlocked_at IS NOT NULL
	ORDER BY ua.unlocked_at DESC
	`
	rows, err := r.db.Query(ctx, q, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []*entity.Achievement
	for rows.Next() {
		var a entity.Achievement
		var typeStr string
		if err := rows.Scan(&a.ID, &typeStr, &a.Name, &a.Description, &a.IconURL, &a.RequiredCount, &a.Tag, &a.Progress); err != nil {
			return nil, err
		}
		a.Type = entity.AchievementType(typeStr)
		list = append(list, &a)
	}
	return list, rows.Err()
}

func (r *achievementRepo) UpdateProgress(ctx context.Context, ua *entity.UserAchievement) error {
	q := `
	INSERT INTO tour_guide.user_achievements (user_id, achievement_id, progress, unlocked_at)
	VALUES ($1, $2, $3, 
			CASE WHEN $3 >= (SELECT required_count FROM tour_guide.achievements WHERE id = $2) 
				THEN NOW() ELSE NULL END)
	ON CONFLICT (user_id, achievement_id) DO UPDATE
	SET progress = EXCLUDED.progress,
		unlocked_at = CASE WHEN EXCLUDED.progress >= (SELECT required_count FROM tour_guide.achievements WHERE id = EXCLUDED.achievement_id) 
							THEN COALESCE(tour_guide.user_achievements.unlocked_at, NOW()) 
							ELSE NULL END
	`
	_, err := r.db.Exec(ctx, q, ua.UserID, ua.AchievementID, ua.Progress)
	return err
}

func (r *achievementRepo) GetByIDs(ctx context.Context, ids []int) ([]*entity.Achievement, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	q := `
	SELECT id, type, name, COALESCE(description,''), 
			COALESCE(icon_url,''), required_count, COALESCE(tag,'')
	FROM tour_guide.achievements
	WHERE id = ANY($1)
	`
	rows, err := r.db.Query(ctx, q, ids)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []*entity.Achievement
	for rows.Next() {
		var a entity.Achievement
		var typeStr string
		if err := rows.Scan(&a.ID, &typeStr, &a.Name, &a.Description, &a.IconURL, &a.RequiredCount, &a.Tag); err != nil {
			return nil, err
		}
		a.Type = entity.AchievementType(typeStr)
		list = append(list, &a)
	}
	return list, rows.Err()
}
