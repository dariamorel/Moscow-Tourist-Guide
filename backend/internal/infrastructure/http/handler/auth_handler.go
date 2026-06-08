package handler

import (
	"net/http"

	"tour-guide/internal/application/service"
	"tour-guide/internal/domain"
	"tour-guide/internal/infrastructure/http/mappers"
	"tour-guide/internal/infrastructure/http/api"
)

type AuthHandler struct {
 	service *service.AuthService
}

func NewAuthHandler(svc *service.AuthService) *AuthHandler {
 	return &AuthHandler{service: svc}
}

func (h *AuthHandler) Registration(w http.ResponseWriter, r *http.Request) {
	var input api.UserInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	user, err := h.service.Register(r.Context(), service.RegisterInput{
		Login:    input.Login,
		Password: input.Password,
	})
	if err != nil {
		if err == domain.ErrAlreadyExists {
			respondError(w, http.StatusConflict, "Login is already taken")
			return
		}
		respondError(w, http.StatusBadRequest, "Invalid user data")
		return
	}
	respondJSON(w, http.StatusCreated, mappers.ToAPIUser(user))
}

func (h *AuthHandler) Login(w http.ResponseWriter, r *http.Request) {
	var input api.UserInput
	if err := decodeJSON(r, &input); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	user, err := h.service.Login(r.Context(), service.LoginInput{
		Login:    input.Login,
		Password: input.Password,
	})
	if err != nil {
		if err == domain.ErrUnauthorized {
			respondError(w, http.StatusUnauthorized, "Invalid credentials")
			return
		}
		respondError(w, http.StatusInternalServerError, "Login failed")
		return
	}

	respondJSON(w, http.StatusOK, map[string]any{
		"token": "jwt_placeholder_replace_with_real_service",
		"user":  mappers.ToAPIUser(user),
	})
}
