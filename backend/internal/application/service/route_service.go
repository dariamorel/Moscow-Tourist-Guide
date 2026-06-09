package service

import (
	"context"
	"log"
	"time"
	"fmt"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
	"tour-guide/internal/domain/value_object"
	domainservices "tour-guide/internal/domain/service"
)

type CreateRouteInput struct {
	UserID       int
	PlannedOrder []int
}

type UpdateRouteInput struct {
	Order        []int
	PlannedOrder []int
	TotalTime    string
	Date         time.Time
	Status       entity.RouteStatus
}

type UpdateRouteData struct {
	Order        *[]int
	PlannedOrder *[]int
	TotalTime    *string
	Status       *entity.RouteStatus
}

type ListRoutesInput struct {
	UserID int
	Status *entity.RouteStatus
}

type RouteService struct {
	routeRepo repository.RouteRepository
	sightRepo repository.SightRepository
	achievementSvc *AchievementService
	optimizer domainservices.RouteOptimizer
}

func NewRouteService(rRepo repository.RouteRepository, sRepo repository.SightRepository, aSvc *AchievementService, optimizer domainservices.RouteOptimizer) *RouteService {
 	return &RouteService{routeRepo: rRepo, sightRepo: sRepo, achievementSvc: aSvc, optimizer: optimizer}
}

func (s *RouteService) CreateRoute(ctx context.Context, in CreateRouteInput) (*entity.Route, error) {
	if in.UserID <= 0 || len(in.PlannedOrder) < 2 {
		return nil, domain.ErrInvalidInput
	}

	var totalTime time.Duration
	for i := 0; i < len(in.PlannedOrder)-1; i++ {
		t, err := s.optimizer.GetTravelTime(ctx, in.PlannedOrder[i], in.PlannedOrder[i+1])
		if err != nil {
			return nil, fmt.Errorf("failed to calculate travel time: %w", err)
		}
		totalTime += t
	}

	route := &entity.Route{
		UserID:       in.UserID,
		PlannedOrder: in.PlannedOrder,
		TotalTime:    valueobject.FormatDuration(totalTime),
		Date:         time.Now(),
		Status:       entity.StatusPlanned,
	}

	if err := route.Validate(); err != nil {
		return nil, err
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

func (s *RouteService) UpdateRoute(ctx context.Context, routeID int, data UpdateRouteData) (*entity.Route, error) {
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

	oldStatus := route.Status

	if data.Order != nil {
		route.Order = *data.Order
	}
	if data.PlannedOrder != nil {
		route.PlannedOrder = *data.PlannedOrder
	}
	if data.TotalTime != nil {
		route.TotalTime = *data.TotalTime
	}
	if data.Status != nil {
		route.Status = *data.Status
	}

	if err := route.Validate(); err != nil {
		return nil, err
	}

	if err := s.routeRepo.Update(ctx, route); err != nil {
		return nil, err
	}

	if route.Status == entity.StatusCompleted && oldStatus != entity.StatusCompleted {
		if err := s.achievementSvc.CheckAndUpdate(ctx, route.UserID); err != nil {
			log.Printf("Error while updating achievements: %s", err.Error())
		}
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
		log.Printf("Delete route: GetByID failed: %s", err.Error())
		return err
	}
	if exists == nil {
		log.Printf("Delete route: Route not found")
		return domain.ErrNotFound
	}
	
	if err := s.routeRepo.Delete(ctx, routeID); err != nil {
		log.Printf("Delete route failed: %s", err.Error())
		return err
	}

	if exists.Status == entity.StatusCompleted {
		log.Printf("Delete route: updating achievements")
		if err := s.achievementSvc.CheckAndUpdate(ctx, exists.UserID); err != nil {
			return fmt.Errorf("Warning: failed to recalculate achievement progress after route deletion: %w", err)
		}
	}

	return nil
}

type RouteWithSights struct {
	*entity.Route
	PlannedSights []*entity.Sight
}

func (s *RouteService) GetRouteWithSights(ctx context.Context, routeID int) (*RouteWithSights, error) {
	route, err := s.routeRepo.GetByID(ctx, routeID)
	if err != nil {
		return nil, err
	}
	if route == nil {
		return nil, domain.ErrNotFound
	}

	sights, err := s.loadSightsByIDs(ctx, route.PlannedOrder)
	if err != nil {
		return nil, err
	}

	return &RouteWithSights{Route: route, PlannedSights: sights}, nil
}

func (s *RouteService) ListRoutesWithSights(ctx context.Context, in ListRoutesInput) ([]*RouteWithSights, error) {
	routes, err := s.routeRepo.List(ctx, repository.RouteFilter{
		UserID: in.UserID,
		Status: in.Status,
	})
	if err != nil {
		return nil, err
	}

	allIDs := make(map[int]struct{})
	for _, r := range routes {
		for _, id := range r.PlannedOrder {
			allIDs[id] = struct{}{}
		}
	}

	idList := make([]int, 0, len(allIDs))
	for id := range allIDs {
		idList = append(idList, id)
	}
	sightsByID, err := s.loadSightsMap(ctx, idList)
	if err != nil {
		return nil, err
	}
	result := make([]*RouteWithSights, 0, len(routes))
	for _, r := range routes {
		sights := make([]*entity.Sight, 0, len(r.PlannedOrder))
		for _, id := range r.PlannedOrder {
			if s, ok := sightsByID[id]; ok {
				sights = append(sights, s)
			}
		}
		result = append(result, &RouteWithSights{Route: r, PlannedSights: sights})
	}
	return result, nil
}

func (s *RouteService) loadSightsByIDs(ctx context.Context, ids []int) ([]*entity.Sight, error) {
	sightsMap, err := s.loadSightsMap(ctx, ids)
	if err != nil {
		return nil, err
	}
	result := make([]*entity.Sight, 0, len(ids))
	for _, id := range ids {
		if s, ok := sightsMap[id]; ok {
			result = append(result, s)
		}
	}
	return result, nil
}

func (s *RouteService) loadSightsMap(ctx context.Context, ids []int) (map[int]*entity.Sight, error) {
	result := make(map[int]*entity.Sight)
	for _, id := range ids {
		if _, exists := result[id]; exists {
			continue
		}
		sight, err := s.sightRepo.GetByID(ctx, id)
		if err != nil {
			return nil, err
		}
		if sight != nil {
			result[id] = sight
		}
	}
	return result, nil
}
