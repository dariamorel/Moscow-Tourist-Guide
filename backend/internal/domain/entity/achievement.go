package entity

import(
	"fmt"
	"time"
	"tour-guide/internal/domain"
)

type Achievement struct {
	ID            int
	Type          string
	Name          string
	Description   string
	IconURL       string
	RequiredCount int
	Tag           string
}

type UserAchievement struct {
	UserID        int
	AchievementID int
	Progress      int
	UnlockedAt    *time.Time
}

func (ua *UserAchievement) Validate() error {
	if ua.UserID <= 0 || ua.AchievementID <= 0 {
		return fmt.Errorf("%w: user and achievement ids must be positive", domain.ErrInvalidInput)
	}
	if ua.Progress < 0 {
		return fmt.Errorf("%w: progress cannot be negative", domain.ErrInvalidInput)
	}
	return nil
}
