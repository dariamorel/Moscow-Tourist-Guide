package handler

import (
	"net/http"
	"log"

	"tour-guide/internal/application/service"
	"tour-guide/internal/domain"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type SightHandler struct {
 	service *service.SightService
}

func NewSightHandler(svc *service.SightService) *SightHandler {
 	return &SightHandler{service: svc}
}

func (h *SightHandler) GetSight(w http.ResponseWriter, r *http.Request, sightId int) {
	log.Printf("GetSight: id=%d", sightId)
	
	sight, err := h.service.GetSight(r.Context(), sightId)
	if err != nil {
		log.Printf("GetSight: service error: %T - %v", err, err)
		if err == domain.ErrNotFound {
			respondError(w, http.StatusNotFound, "Sight not found")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to fetch sight")
		return
	}
	log.Printf("GetSight: success, short_desc=%q", sight.ShortDesc)
	respondJSON(w, http.StatusOK, mappers.ToAPISight(sight))
}

func (h *SightHandler) GetSights(w http.ResponseWriter, r *http.Request, params api.GetSightsParams) {
	filter := service.SightFilter{
		Limit:  mappers.Val(params.Limit, 20),
		Offset: mappers.Val(params.Offset, 0),
		Tag:    params.Tag,
	}
	log.Printf("GetSights: limit=%d, offset=%d, tag=%v", filter.Limit, filter.Offset, filter.Tag)

	sights, err := h.service.ListSights(r.Context(), filter)
	if err != nil {
		log.Printf("GetSights: service error: %v", err)
		respondError(w, http.StatusInternalServerError, "Failed to list sights")
		return
	}
	log.Printf("GetSights: returned %d sights", len(sights))
	respondJSON(w, http.StatusOK, mappers.ToAPISights(sights))
}
