package handler

import (
	"net/http"
	"log"

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
		log.Printf("CreateRoute: JSON decode error: %v", err)
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}
	
	log.Printf("CreateRoute: input.UserId=%d, len(PlannedOrder)=%d", input.UserId, len(input.PlannedOrder))

	route, err := mappers.RouteInputToDomain(input)
	if err != nil {
		log.Printf("CreateRoute: mapper error: %v", err)
		respondError(w, http.StatusBadRequest, "Invalid route data")
		return
	}

	created, err := h.service.CreateRoute(r.Context(), service.CreateRouteInput{
		UserID:       route.UserID,
		PlannedOrder: route.PlannedOrder,
		TotalTime:    route.TotalTime,
		Date:         route.Date,
	})
	if err != nil {
		log.Printf("CreateRoute: service error: %T - %v", err, err)
		if err == domain.ErrInvalidInput {
			respondError(w, http.StatusBadRequest, "Invalid route data")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to create route")
		return
	}
	log.Printf("CreateRoute: success, route_id=%d", created.ID)
	respondJSON(w, http.StatusCreated, mappers.ToAPIRoute(created))
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
		Order:        &domainRoute.Order,
		PlannedOrder: &domainRoute.PlannedOrder,
		TotalTime:    &domainRoute.TotalTime,
		Status:       &domainRoute.Status,
	}

	updated, err := h.service.UpdateRoute(r.Context(), domainRoute.ID, updateData)
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
