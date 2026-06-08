package mappers

import (
	"time"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIRoute(r *entity.Route) api.Route {
	statusStr := string(r.Status)
	return api.Route{
		Id:           int(r.ID),
		UserId:       int(r.UserID),
		Date:         r.Date,
		Order:        Ptr(r.Order),
		PlannedOrder: Ptr(r.PlannedOrder),
		TotalTime:    EmptyStringToNil(r.TotalTime),
		Status:       Ptr(api.RouteStatus(statusStr)),
	}
}

func ToAPIRoutes(routes []*entity.Route) []api.Route {
	res := make([]api.Route, 0, len(routes))
	for _, r := range routes {
		res = append(res, ToAPIRoute(r))
	}
	return res
}

func RouteInputToDomain(in api.RouteInput) (*entity.Route, error) {
	route := &entity.Route{
		UserID:       in.UserId,
		PlannedOrder: in.PlannedOrder,
		TotalTime:    Val(in.TotalTime, ""),
		Date:         time.Now(),
		Status:       entity.StatusPlanned,
	}

	if err := route.Validate(); err != nil {
		return nil, domain.ErrInvalidInput
	}
	return route, nil
}

func UpdateRouteInputToDomain(routeID int, in api.RouteInput, existing *entity.Route) (*entity.Route, error) {
	route := &entity.Route{
		ID:           routeID,
		UserID:       existing.UserID,
		Order:        existing.Order,
		PlannedOrder: in.PlannedOrder,
		TotalTime:    Val(in.TotalTime, existing.TotalTime),
		Date:         existing.Date,
		Status:       existing.Status,
	}

	if err := route.Validate(); err != nil {
		return nil, domain.ErrInvalidInput
	}
	return route, nil
}

func RouteUpdateInputToDomain(routeID int, in api.RouteUpdateInput, existing *entity.Route) (*entity.Route, error) {
	route := &entity.Route{
		ID:           routeID,
		UserID:       existing.UserID,
		Order:        existing.Order,
		PlannedOrder: existing.PlannedOrder,
		TotalTime:    existing.TotalTime,
		Date:         existing.Date,
		Status:       existing.Status,
	}

	if in.Order != nil {
		route.Order = *in.Order
	}
	if in.PlannedOrder != nil {
		route.PlannedOrder = *in.PlannedOrder
	}
	if in.TotalTime != nil {
		route.TotalTime = *in.TotalTime
	}
	if in.Status != nil {
		route.Status = entity.RouteStatus(*in.Status)
	}

	if err := route.Validate(); err != nil {
		return nil, domain.ErrInvalidInput
	}
	return route, nil
}

func ParseRouteStatus(s *api.RouteStatus) (entity.RouteStatus, error) {
	if s == nil {
		return "", domain.ErrInvalidInput
	}
	switch entity.RouteStatus(*s) {
	case entity.StatusPlanned, entity.StatusInProgress, entity.StatusCompleted, entity.StatusDeclined:
		return entity.RouteStatus(*s), nil
	default:
		return "", domain.ErrInvalidInput
	}
}

func ParseRouteStatusFromParams(s *api.GetRoutesParamsStatus) (entity.RouteStatus, error) {
	if s == nil {
		return "", domain.ErrInvalidInput
	}
	switch entity.RouteStatus(*s) {
	case entity.StatusPlanned, entity.StatusInProgress, entity.StatusCompleted, entity.StatusDeclined:
		return entity.RouteStatus(*s), nil
	default:
		return "", domain.ErrInvalidInput
	}
}

func ParseRouteUpdateStatus(s *api.RouteUpdateInputStatus) (entity.RouteStatus, error) {
	if s == nil {
		return "", domain.ErrInvalidInput
	}
	switch entity.RouteStatus(*s) {
	case entity.StatusPlanned, entity.StatusInProgress, entity.StatusCompleted, entity.StatusDeclined:
		return entity.RouteStatus(*s), nil
	default:
		return "", domain.ErrInvalidInput
	}
}

func ToAPIRouteUpdateStatus(s entity.RouteStatus) api.RouteUpdateInputStatus {
 	return api.RouteUpdateInputStatus(s)
}
