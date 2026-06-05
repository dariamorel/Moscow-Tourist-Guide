package service

import (
	"context"
	"time"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type CreateRouteInput struct {
	UserID       int
	PlannedOrder []int
	TotalTime    string
	Date         time.Time
}

type UpdateRouteInput struct {
	Order        []int
	PlannedOrder []int
	TotalTime    string
	Date         time.Time
	Status       entity.RouteStatus
}

type ListRoutesInput struct {
	UserID int
	Status *entity.RouteStatus
}

type RouteService struct {
	routeRepo repository.RouteRepository
	sightRepo repository.SightRepository
}

func NewRouteService(rRepo repository.RouteRepository, sRepo repository.SightRepository) *RouteService {
 	return &RouteService{routeRepo: rRepo, sightRepo: sRepo}
}

func (s *RouteService) CreateRoute(ctx context.Context, in CreateRouteInput) (*entity.Route, error) {
	if in.UserID <= 0 {
		return nil, domain.ErrInvalidInput
	}
	if len(in.PlannedOrder) == 0 {
		return nil, domain.ErrInvalidInput
	}
	if in.Date.IsZero() {
		return nil, domain.ErrInvalidInput
	}

	route := &entity.Route{
		UserID:       in.UserID,
		PlannedOrder: in.PlannedOrder,
		TotalTime:    in.TotalTime,
		Date:         in.Date,
		Status:       entity.StatusPlanned,
	}

	if err := route.Validate(); err != nil {
		return nil, err
	}

	for _, sightID := range in.PlannedOrder {
		if _, err := s.sightRepo.GetByID(ctx, sightID); err != nil {
			return nil, domain.ErrInvalidInput
		}
	}

	if err := s.routeRepo.Create(ctx, route); err != nil {
		return nil, err
	}
	return route, nil
}

func (s *RouteService) GetRoute(ctx context.Context, id int) (*entity.Route, error) {
    if id <= 0 { return nil, domain.ErrInvalidInput }
    route, err := s.routeRepo.GetByID(ctx, id)
    if err != nil { return nil, err }
    if route == nil { return nil, domain.ErrNotFound }
    return route, nil
}

func (s *RouteService) UpdateRoute(ctx context.Context, routeID int, in UpdateRouteInput) (*entity.Route, error) {
	if routeID <= 0 {
		return nil, domain.ErrInvalidInput
	}

	route, err := s.routeRepo.GetByID(ctx, routeID)
	if err != nil {
		return nil, err
	}
	if route == nil {
		return nil, domain.ErrNotFound
	}

	if len(in.PlannedOrder) > 0 {
		route.PlannedOrder = in.PlannedOrder
	}
	if in.Order != nil {
		route.Order = in.Order
	}
	if in.TotalTime != "" {
		route.TotalTime = in.TotalTime
	}
	if !in.Date.IsZero() {
		route.Date = in.Date
	}
	if in.Status != "" {
		route.Status = in.Status
	}

	if err := route.Validate(); err != nil {
		return nil, err
	}

	if err := s.routeRepo.Update(ctx, route); err != nil {
		return nil, err
	}
	return route, nil
}

func (s *RouteService) ListRoutes(ctx context.Context, in ListRoutesInput) ([]*entity.Route, error) {
	if in.UserID <= 0 {
		return nil, domain.ErrInvalidInput
	}
	return s.routeRepo.List(ctx, repository.RouteFilter{
		UserID: in.UserID,
		Status: in.Status,
	})
}

func (s *RouteService) DeleteRoute(ctx context.Context, routeID int) error {
	if routeID <= 0 {
		return domain.ErrInvalidInput
	}

	exists, err := s.routeRepo.GetByID(ctx, routeID)
	if err != nil {
		return err
	}
	if exists == nil {
		return domain.ErrNotFound
	}
	return s.routeRepo.Delete(ctx, routeID)
}
