package handler

import (
	"net/http"

	"tour-guide/internal/application/service"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type AchievementHandler struct {
 	service *service.AchievementService
}

func NewAchievementHandler(svc *service.AchievementService) *AchievementHandler {
 	return &AchievementHandler{service: svc}
}

func (h *AchievementHandler) GetAchievements(w http.ResponseWriter, r *http.Request, params api.GetAchievementsParams) {
	var userID int
	if params.UserId != nil {
		userID = *params.UserId
	}

	achievements, err := h.service.ListAll(r.Context(), userID)
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
