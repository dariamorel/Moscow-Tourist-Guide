package repository

import (
	"context"
	"tour-guide/internal/domain/entity"
)

type RouteFilter struct {
	UserID int64
	Status *entity.RouteStatus
}

type RouteRepository interface {
	Create(ctx context.Context, route *entity.Route) error
	
	Update(ctx context.Context, route *entity.Route) error
	
	GetByID(ctx context.Context, id int64) (*entity.Route, error)
	
	List(ctx context.Context, filter RouteFilter) ([]*entity.Route, error)
	
	Delete(ctx context.Context, id int64) error
}
