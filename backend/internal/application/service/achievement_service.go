package service

import (
	"context"
	"log"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type AchievementService struct {
	repo      repository.AchievementRepository
	sightRepo repository.SightRepository
	routeRepo repository.RouteRepository
}

func NewAchievementService(
	repo repository.AchievementRepository,
	sightRepo repository.SightRepository,
	routeRepo repository.RouteRepository,
) *AchievementService {
 	return &AchievementService{repo: repo, sightRepo: sightRepo, routeRepo: routeRepo}
}

func (s *AchievementService) ListAll(ctx context.Context, userID int) ([]*entity.Achievement, error) {
 	return s.repo.ListAll(ctx, userID)
}

func (s *AchievementService) ListCompletedByUser(ctx context.Context, userID int) ([]*entity.Achievement, error) {
	if userID <= 0 {
		return nil, domain.ErrInvalidInput
	}
	return s.repo.GetCompletedByUserID(ctx, userID)
}

func (s *AchievementService) CheckAndUpdate(ctx context.Context, userID int) error {
    log.Printf("CheckAndUpdate: userID=%d", userID)
    
    if userID <= 0 {
        return domain.ErrInvalidInput
    }

    stats, err := s.collectUserStats(ctx, userID)
    if err != nil {
        log.Printf("CheckAndUpdate: collectUserStats failed: %v", err)
        return err
    }

    log.Printf("CheckAndUpdate: stats.CompletedRoutes=%d, stats.TotalUniqueSights=%d", 
        stats.CompletedRoutes, stats.TotalUniqueSights)

    allAchievements, err := s.repo.ListAll(ctx, 0)
    if err != nil {
        log.Printf("CheckAndUpdate: ListAll failed: %v", err)
        return err
    }

    for _, ach := range allAchievements {
        var progress int
        switch ach.Type {
        case entity.AchievementSightsVisited:
            progress = stats.TotalUniqueSights
        case entity.AchievementRoutesCompleted:
            progress = stats.CompletedRoutes
        case entity.AchievementTagCollector:
            progress = stats.SightsByTag[ach.Tag]
        default:
            continue
        }

        log.Printf("CheckAndUpdate: achievement %d (%s), progress=%d", ach.ID, ach.Type, progress)

        ua := &entity.UserAchievement{
            UserID:        userID,
            AchievementID: ach.ID,
            Progress:      progress,
        }
        if err := s.repo.UpdateProgress(ctx, ua); err != nil {
            log.Printf("CheckAndUpdate: UpdateProgress failed for achievement %d: %v", ach.ID, err)
            return err
        }
    }
    
    log.Printf("CheckAndUpdate: completed successfully")
    return nil
}

type userStats struct {
	TotalUniqueSights int
	CompletedRoutes   int
	SightsByTag       map[string]int
}

func (s *AchievementService) collectUserStats(ctx context.Context, userID int) (*userStats, error) {
	stats := &userStats{
		SightsByTag: make(map[string]int),
	}

	completedStatus := entity.StatusCompleted
	routes, err := s.routeRepo.List(ctx, repository.RouteFilter{
		UserID: userID,
		Status: &completedStatus,
	})
	if err != nil {
		return nil, err
	}

	stats.CompletedRoutes = len(routes)

	uniqueSights := make(map[int]struct{})
	for _, r := range routes {
		for _, sightID := range r.Order {
			uniqueSights[sightID] = struct{}{}
		}
	}
	stats.TotalUniqueSights = len(uniqueSights)

	for sightID := range uniqueSights {
		sight, err := s.sightRepo.GetByID(ctx, sightID)
		if err != nil || sight == nil {
			continue
		}
		for _, tag := range sight.Tags {
			stats.SightsByTag[tag]++
		}
	}

	return stats, nil
}
