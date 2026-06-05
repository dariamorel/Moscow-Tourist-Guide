package service

import (
	"context"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type UpdateAchievementInput struct {
	UserID        int
	AchievementID int
	Progress      int
}

type AchievementService struct {
 	repo repository.AchievementRepository
}

func NewAchievementService(repo repository.AchievementRepository) *AchievementService {
 	return &AchievementService{repo: repo}
}

func (s *AchievementService) ListAll(ctx context.Context) ([]*entity.Achievement, error) {
 	return s.repo.ListAll(ctx)
}

func (s *AchievementService) ListCompletedByUser(ctx context.Context, userID int) ([]*entity.Achievement, error) {
	if userID <= 0 {
		return nil, domain.ErrInvalidInput
	}
	return s.repo.GetCompletedByUserID(ctx, userID)
}

func (s *AchievementService) UpdateProgress(ctx context.Context, in UpdateAchievementInput) error {
	if in.UserID <= 0 || in.AchievementID <= 0 {
		return domain.ErrInvalidInput
	}
	if in.Progress < 0 {
		return domain.ErrInvalidInput
	}

	ua := &entity.UserAchievement{
		UserID:        in.UserID,
		AchievementID: in.AchievementID,
		Progress:      in.Progress,
	}

	if err := ua.Validate(); err != nil {
		return err
	}

	return s.repo.SaveUserProgress(ctx, ua)
}
