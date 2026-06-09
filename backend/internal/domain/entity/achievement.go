package entity

import(
	"fmt"
	"time"
	"tour-guide/internal/domain"
)

type AchievementType string

const (
	AchievementSightsVisited AchievementType = "sights_visited"
	AchievementRoutesCompleted AchievementType = "routes_completed"
	AchievementTagCollector AchievementType = "tag_collector"
)

type Achievement struct {
	ID            int
	Type          AchievementType
	Name          string
	Description   string
	IconURL       string
	RequiredCount int
	Tag           string
	Progress      int
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
