package handler

import (
	"net/http"
	"log"

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
		log.Printf("Registration: JSON decode error: %v", err)
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}
	
	log.Printf("Registration: login=%q, password_len=%d", input.Login, len(input.Password))

	user, err := h.service.Register(r.Context(), service.RegisterInput{
		Login:    input.Login,
		Password: input.Password,
	})

	if err != nil {
		log.Printf("Registration: service error: %T - %v", err, err)
		if err == domain.ErrAlreadyExists {
			respondError(w, http.StatusConflict, "Login is already taken")
			return
		}
		respondError(w, http.StatusBadRequest, "Invalid user data")
		return
	}
	log.Printf("Registration: success, user_id=%d", user.ID)
	respondJSON(w, http.StatusCreated, mappers.ToAPIUser(user))
}

func (h *AuthHandler) Login(w http.ResponseWriter, r *http.Request) {
	var input api.UserInput
	if err := decodeJSON(r, &input); err != nil {
		log.Printf("Login: JSON decode error: %v", err)
		respondError(w, http.StatusBadRequest, "Invalid request body")
		return
	}

	log.Printf("Login: attempting login=%q", input.Login)
	
	user, err := h.service.Login(r.Context(), service.LoginInput{
	Login:    input.Login,
	Password: input.Password,
	})
	if err != nil {
		log.Printf("Login: service error: %T - %v", err, err)
		if err == domain.ErrUnauthorized {
			respondError(w, http.StatusUnauthorized, "Invalid credentials")
			return
		}
		respondError(w, http.StatusInternalServerError, "Login failed")
		return
	}

	log.Printf("Login: success, user_id=%d", user.ID)
	respondJSON(w, http.StatusOK, map[string]any{
		"token": "jwt_placeholder",
		"user":  mappers.ToAPIUser(user),
	})
}
