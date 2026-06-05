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

func (r *achievementRepo) ListAll(ctx context.Context) ([]*entity.Achievement, error) {
	q := `SELECT id, type, name, COALESCE(description,''), COALESCE(icon_url,''), required_count, COALESCE(tag,'') FROM achievements`
	rows, err := r.db.Query(ctx, q)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []*entity.Achievement
	for rows.Next() {
		var a entity.Achievement
		if err := rows.Scan(&a.ID, &a.Type, &a.Name, &a.Description, &a.IconURL, &a.RequiredCount, &a.Tag); err != nil {
			return nil, err
		}
		list = append(list, &a)
	}
	return list, rows.Err()
}

func (r *achievementRepo) GetCompletedByUserID(ctx context.Context, userID int) ([]*entity.Achievement, error) {
	q := `
	SELECT a.id, a.type, a.name, COALESCE(a.description,''), COALESCE(a.icon_url,''), a.required_count, COALESCE(a.tag,'')
	FROM user_achievements ua
	JOIN achievements a ON ua.achievement_id = a.id
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
		if err := rows.Scan(&a.ID, &a.Type, &a.Name, &a.Description, &a.IconURL, &a.RequiredCount, &a.Tag); err != nil {
			return nil, err
		}
		list = append(list, &a)
	}
	return list, rows.Err()
}

func (r *achievementRepo) SaveUserProgress(ctx context.Context, ua *entity.UserAchievement) error {
	q := `
	INSERT INTO user_achievements (user_id, achievement_id, progress, unlocked_at)
	VALUES ($1, $2, $3, CASE WHEN $3 >= (SELECT required_count FROM achievements WHERE id = $2) THEN NOW() ELSE NULL END)
	ON CONFLICT (user_id, achievement_id) DO UPDATE
	SET progress = EXCLUDED.progress,
		unlocked_at = CASE WHEN EXCLUDED.progress >= (SELECT required_count FROM achievements WHERE id = EXCLUDED.achievement_id) 
							THEN COALESCE(EXCLUDED.unlocked_at, NOW()) 
							ELSE NULL END
	`
	_, err := r.db.Exec(ctx, q, ua.UserID, ua.AchievementID, ua.Progress)
	return err
}
