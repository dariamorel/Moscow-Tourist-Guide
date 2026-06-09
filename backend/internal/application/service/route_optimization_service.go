package service

import (
	"context"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
	domainservices "tour-guide/internal/domain/service"
	"tour-guide/internal/domain/value_object"
)

type OptimizeRouteInput struct {
 	SightIDs []int
}

type OptimizeRouteOutput struct {
	OptimizedOrder []*entity.Sight
	TotalTime      string
}

type RouteOptimizationService struct {
	sightRepo repository.SightRepository
	optimizer domainservices.RouteOptimizer
}

func NewRouteOptimizationService(
	sightRepo repository.SightRepository,
	optimizer domainservices.RouteOptimizer,
) *RouteOptimizationService {
 	return &RouteOptimizationService{sightRepo: sightRepo, optimizer: optimizer}
}

func (s *RouteOptimizationService) Optimize(ctx context.Context, in OptimizeRouteInput) (*OptimizeRouteOutput, error) {
	if len(in.SightIDs) < 2 {
		return nil, domain.ErrInvalidInput
	}

	sights := make([]*entity.Sight, 0, len(in.SightIDs))
	seen := make(map[int]bool)
	for _, id := range in.SightIDs {
		if seen[id] {
			continue
		}
		seen[id] = true

		sight, err := s.sightRepo.GetByID(ctx, id)
		if err != nil {
			return nil, err
		}
		if sight == nil {
			return nil, domain.ErrNotFound
		}
		sights = append(sights, sight)
	}

	if len(sights) < 2 {
		return nil, domain.ErrInvalidInput
	}

	result, err := s.optimizer.Optimize(ctx, sights)
	if err != nil {
		return nil, err
	}

	return &OptimizeRouteOutput{
		OptimizedOrder: result.OptimizedOrder,
		TotalTime:      valueobject.FormatDuration(result.TotalTime),
	}, nil
}
