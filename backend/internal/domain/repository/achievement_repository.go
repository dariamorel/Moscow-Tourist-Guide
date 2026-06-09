package repository

import (
	"context"
	"tour-guide/internal/domain/entity"
)

type AchievementRepository interface {
	ListAll(ctx context.Context, userID int) ([]*entity.Achievement, error)
	
	GetCompletedByUserID(ctx context.Context, userID int) ([]*entity.Achievement, error)
	
	UpdateProgress(ctx context.Context, ua *entity.UserAchievement) error

	GetByIDs(ctx context.Context, ids []int) ([]*entity.Achievement, error)
}
