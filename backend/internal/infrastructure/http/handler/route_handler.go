package handlers

import (
	"time"
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

	route, err := mappers.RouteInputToDomain(input, time.Now())
	if err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}

	created, err := h.service.CreateRoute(r.Context(), service.CreateRouteInput{
		UserID:       route.UserID,
		PlannedOrder: route.PlannedOrder,
		TotalTime:    route.TotalTime,
		Date:         route.Date,
	})
	if err != nil {
	if err == domain.ErrInvalidInput {
	respondError(w, http.StatusBadRequest, "Invalid route data")
	return
	}
	respondError(w, http.StatusInternalServerError, "Failed to create route")
	return
	}
	respondJSON(w, http.StatusCreated, mappers.ToAPIRoute(created))
}

func (h *RouteHandler) UpdateRoute(w http.ResponseWriter, r *http.Request, routeId int) {
	var input api.RouteInput
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

	domainRoute, err := mappers.UpdateRouteInputToDomain(routeId, input, existing)
	if err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}

	updated, err := h.service.UpdateRoute(r.Context(), domainRoute.ID, service.UpdateRouteInput{
		Order:        domainRoute.Order,
		PlannedOrder: domainRoute.PlannedOrder,
		TotalTime:    domainRoute.TotalTime,
		Date:         domainRoute.Date,
		Status:       domainRoute.Status,
	})
	if err != nil {
		if err == domain.ErrNotFound {
			respondError(w, http.StatusNotFound, "Route not found")
			return
		}
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoute(updated))
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
		s, err := mappers.ParseRouteStatusFromParams(params.Status)
		if err != nil {
			respondError(w, http.StatusBadRequest, "Invalid status filter")
			return
		}
		status = &s
	}

	routes, err := h.service.ListRoutes(r.Context(), service.ListRoutesInput{
		UserID: params.UserId,
		Status: status,
	})
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list routes")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoutes(routes))
}

func (h *RouteHandler) GetRoutesPassed(w http.ResponseWriter, r *http.Request, params api.GetRoutesPassedParams) {
	completed := entity.StatusCompleted
	routes, err := h.service.ListRoutes(r.Context(), service.ListRoutesInput{
		UserID: params.UserId,
		Status: &completed,
	})
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list passed routes")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoutes(routes))
}

func (h *RouteHandler) GetRoutesDeclined(w http.ResponseWriter, r *http.Request, params api.GetRoutesDeclinedParams) {
	declined := entity.StatusDeclined
	routes, err := h.service.ListRoutes(r.Context(), service.ListRoutesInput{
		UserID: params.UserId,
		Status: &declined,
	})
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list declined routes")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoutes(routes))
}

func (h *RouteHandler) GetRoutesInProgress(w http.ResponseWriter, r *http.Request, params api.GetRoutesInProgressParams) {
	inProgress := entity.StatusInProgress
	routes, err := h.service.ListRoutes(r.Context(), service.ListRoutesInput{
		UserID: params.UserId,
		Status: &inProgress,
	})
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list in-progress routes")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIRoutes(routes))
}
