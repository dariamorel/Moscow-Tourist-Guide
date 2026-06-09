package service

import (
	"context"
	"time"

	"tour-guide/internal/domain/entity"
)

type OptimizationResult struct {
	OptimizedOrder []*entity.Sight
	TotalTime      time.Duration
}

type RouteOptimizer interface {
 	Optimize(ctx context.Context, sights []*entity.Sight) (*OptimizationResult, error)
	GetTravelTime(ctx context.Context, sight1ID, sight2ID int) (time.Duration, error)
}
