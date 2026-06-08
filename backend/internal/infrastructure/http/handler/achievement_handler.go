package handler

import (
	"net/http"

	"tour-guide/internal/application/service"
	"tour-guide/internal/domain"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type AchievementHandler struct {
 	service *service.AchievementService
}

func NewAchievementHandler(svc *service.AchievementService) *AchievementHandler {
 	return &AchievementHandler{service: svc}
}

func (h *AchievementHandler) GetAchievements(w http.ResponseWriter, r *http.Request) {
	achievements, err := h.service.ListAll(r.Context())
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list achievements")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIAchievements(achievements))
}

func (h *AchievementHandler) GetAchievementsCompleted(w http.ResponseWriter, r *http.Request, params api.GetAchievementsCompletedParams) {
	achievements, err := h.service.ListCompletedByUser(r.Context(), params.UserId)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to list completed achievements")
		return
	}
	respondJSON(w, http.StatusOK, mappers.ToAPIAchievements(achievements))
}

func (h *AchievementHandler) UpdateAchievements(w http.ResponseWriter, r *http.Request) {
	var input api.UserAchievementInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	ua := mappers.UserAchievementInputToEntity(input)
	if err := h.service.UpdateProgress(r.Context(), service.UpdateAchievementInput{
		UserID:        ua.UserID,
		AchievementID: ua.AchievementID,
		Progress:      ua.Progress,
	}); err != nil {
		if err == domain.ErrInvalidInput {
			respondError(w, http.StatusBadRequest, "Invalid achievement data")
			return
		}
		respondError(w, http.StatusInternalServerError, "Failed to update progress")
		return
	}
	w.WriteHeader(http.StatusOK)
}
