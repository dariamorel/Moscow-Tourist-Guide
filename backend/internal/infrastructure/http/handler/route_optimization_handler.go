package handler

import (
	"net/http"

	"tour-guide/internal/application/service"
	"tour-guide/internal/domain"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type RouteOptimizationHandler struct {
 	service *service.RouteOptimizationService
}

func NewRouteOptimizationHandler(svc *service.RouteOptimizationService) *RouteOptimizationHandler {
 	return &RouteOptimizationHandler{service: svc}
}

func (h *RouteOptimizationHandler) OptimizeRoute(w http.ResponseWriter, r *http.Request) {
	var input api.OptimizeRouteInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	if len(input.SightIds) > 20 {
		respondError(w, http.StatusBadRequest, "Maximum 20 sights allowed for optimization")
		return
	}
	if len(input.SightIds) < 2 {
		respondError(w, http.StatusBadRequest, "At least 2 sight IDs are required")
		return
	}

	ids := make([]int, len(input.SightIds))
	for i, id := range input.SightIds {
		ids[i] = id
	}

	result, err := h.service.Optimize(r.Context(), service.OptimizeRouteInput{SightIDs: ids})
	if err != nil {
		if err == domain.ErrNotFound {
			respondError(w, http.StatusNotFound, "One or more sights not found")
			return
		}
		if err == domain.ErrInvalidInput {
			respondError(w, http.StatusBadRequest, err.Error())
			return
		}
		respondError(w, http.StatusInternalServerError, "Optimization failed")
		return
	}

	respondJSON(w, http.StatusOK, mappers.ToAPIOptimizeRouteResponse(result))
}
