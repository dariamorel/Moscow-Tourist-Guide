package mappers

import (
	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/application/service"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIRouteWithSights(r *service.RouteWithSights) api.Route {
	statusStr := string(r.Status)
	sights := ToAPISights(r.PlannedSights)
	return api.Route{
		Id:           int(r.ID),
		UserId:       int(r.UserID),
		Name: 		  Ptr(r.Name),
		Date:         r.Date,
		Order:        Ptr(r.Order),
		PlannedOrder: Ptr(sights),
		TotalTime:    EmptyStringToNil(r.TotalTime),
		Status:       Ptr(api.RouteStatus(statusStr)),
	}
}

func ToAPIRoutesWithSights(routes []*service.RouteWithSights) []api.Route {
	res := make([]api.Route, 0, len(routes))
	for _, r := range routes {
		res = append(res, ToAPIRouteWithSights(r))
	}
	return res
}

/*
func RouteInputToDomain(in api.RouteInput) (*entity.Route, error) {
	route := &entity.Route{
		UserID:       in.UserId,
		PlannedOrder: in.PlannedOrder,
		TotalTime:    "",
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
		TotalTime:    "",
		Date:         existing.Date,
		Status:       existing.Status,
	}

	if err := route.Validate(); err != nil {
		return nil, domain.ErrInvalidInput
	}
	return route, nil
}
*/

func RouteUpdateInputToDomain(routeID int, in api.RouteUpdateInput, existing *entity.Route) (*entity.Route, error) {
	route := &entity.Route{
		ID:           routeID,
		UserID:       existing.UserID,
		Name: 		  existing.Name,
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
	if in.Status != nil {
		route.Status = entity.RouteStatus(*in.Status)
	}
	if in.Name != nil {
		route.Name = *in.Name
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
