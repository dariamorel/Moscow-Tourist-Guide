package repository

import (
	"context"
	"tour-guide/internal/domain/entity"
)

type AchievementRepository interface {
	ListAll(ctx context.Context) ([]*entity.Achievement, error)
	
	GetCompletedByUserID(ctx context.Context, userID int) ([]*entity.Achievement, error)
	
	SaveUserProgress(ctx context.Context, ua *entity.UserAchievement) error
}
