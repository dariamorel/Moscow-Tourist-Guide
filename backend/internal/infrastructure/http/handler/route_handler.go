package handler

import (
	"net/http"

	"tour-guide/internal/application/service"
	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type RouteHandler struct {
 	service *service.RouteService
}

func NewRouteHandler(svc *service.RouteService) *RouteHandler {
 	return &RouteHandler{service: svc}
}

func (h *RouteHandler) CreateRoute(w http.ResponseWriter, r *http.Request) {
	var input api.RouteInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	name := ""
	if input.Name != nil {
		name = *input.Name
	}

	ids := make([]int, len(input.PlannedOrder))
	for i, id := range input.PlannedOrder {
		ids[i] = id
	}

	created, err := h.service.CreateRoute(r.Context(), service.CreateRouteInput{
		UserID:       input.UserId,
		Name: 		  name,
		PlannedOrder: ids,
	})
	if err != nil {
		if err == domain.ErrInvalidInput {
			respondError(w, http.StatusBadRequest, "Invalid route data or missing travel times")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to create route")
		return
	}
	
	withSights, err := h.service.GetRouteWithSights(r.Context(), created.ID)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to load route details")
		return
	}
	respondJSON(w, http.StatusCreated, mappers.ToAPIRouteWithSights(withSights))
}

func (h *RouteHandler) UpdateRoute(w http.ResponseWriter, r *http.Request, routeId int) {
	var input api.RouteUpdateInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	existing, err := h.service.GetRoute(r.Context(), routeId)
	if err != nil {
		if err == domain.ErrNotFound {
			respondError(w, http.StatusNotFound, "Route not found")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to fetch route")
		return
	}

	domainRoute, err := mappers.RouteUpdateInputToDomain(routeId, input, existing)
	if err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}

	updateData := service.UpdateRouteData{
		Name:		  &domainRoute.Name,
		Order:        &domainRoute.Order,
		PlannedOrder: &domainRoute.PlannedOrder,
		TotalTime:    &domainRoute.TotalTime,
		Status:       &domainRoute.Status,
	}

	_, err = h.service.UpdateRoute(r.Context(), domainRoute.ID, updateData)
	if err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}

	withSights, err := h.service.GetRouteWithSights(r.Context(), domainRoute.ID)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to load updated route")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRouteWithSights(withSights))
}

func (h *RouteHandler) DeleteRoute(w http.ResponseWriter, r *http.Request, routeId int) {
	if err := h.service.DeleteRoute(r.Context(), routeId); err != nil {
		if err == domain.ErrNotFound {
			respondError(w, http.StatusNotFound, "Route not found")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to delete route")
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func (h *RouteHandler) GetRoutes(w http.ResponseWriter, r *http.Request, params api.GetRoutesParams) {
	var status *entity.RouteStatus
	if params.Status != nil {
		s := entity.RouteStatus(*params.Status)
		status = &s
	}

	routes, err := h.service.ListRoutesWithSights(r.Context(), service.ListRoutesInput{
		UserID: params.UserId,
		Status: status,
	})
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list routes")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoutesWithSights(routes))
}

func (h *RouteHandler) GetRoutesPassed(w http.ResponseWriter, r *http.Request, params api.GetRoutesPassedParams) {
	completed := api.Completed
	h.GetRoutes(
		w,
		r,
		api.GetRoutesParams{
			UserId: params.UserId,
			Status: &completed,
		},
	)
}

func (h *RouteHandler) GetRoutesDeclined(w http.ResponseWriter, r *http.Request, params api.GetRoutesDeclinedParams) {
	declined := api.Declined
	h.GetRoutes(
		w,
		r,
		api.GetRoutesParams{
			UserId: params.UserId,
			Status: &declined,
		},
	)
}

func (h *RouteHandler) GetRoutesInProgress(w http.ResponseWriter, r *http.Request, params api.GetRoutesInProgressParams) {
	inProgress := api.InProgress
	h.GetRoutes(
		w,
		r,
		api.GetRoutesParams{
			UserId: params.UserId,
			Status: &inProgress,
		},
	)
}

func (h *RouteHandler) GetRoute(w http.ResponseWriter, r *http.Request, routeId int) {
    withSights, err := h.service.GetRouteWithSights(r.Context(), routeId)
    if err != nil {
        if err == domain.ErrNotFound {
            respondError(w, http.StatusNotFound, "Route not found")
            return
        }
        respondError(w, http.StatusInternalServerError, "Failed to fetch route")
        return
    }
    respondJSON(w, http.StatusOK, mappers.ToAPIRouteWithSights(withSights))
}
