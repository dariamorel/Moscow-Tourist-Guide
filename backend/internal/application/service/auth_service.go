package service

import (
	"context"

	"golang.org/x/crypto/bcrypt"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type RegisterInput struct {
	Login    string
	Password string
}

type LoginInput struct {
	Login    string
	Password string
}

type AuthService struct {
 repo repository.UserRepository
}

func NewAuthService(repo repository.UserRepository) *AuthService {
 	return &AuthService{repo: repo}
}

func (s *AuthService) Register(ctx context.Context, in RegisterInput) (*entity.User, error) {
	if in.Login == "" || in.Password == "" {
		return nil, domain.ErrInvalidInput
	}

	exists, err := s.repo.GetByLogin(ctx, in.Login)
	if err != nil {
		return nil, err
	}
	if exists != nil {
		return nil, domain.ErrAlreadyExists
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(in.Password), bcrypt.DefaultCost)
	if err != nil {
		return nil, err
	}

	user := &entity.User{
		Login:        in.Login,
		PasswordHash: string(hash),
	}

	if err := user.Validate(); err != nil {
		return nil, err
	}

	if err := s.repo.Create(ctx, user); err != nil {
		return nil, err
	}
	return user, nil
}

func (s *AuthService) Login(ctx context.Context, in LoginInput) (*entity.User, error) {
	if in.Login == "" || in.Password == "" {
		return nil, domain.ErrUnauthorized
	}

	user, err := s.repo.GetByLogin(ctx, in.Login)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, domain.ErrUnauthorized
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(in.Password)); err != nil {
		return nil, domain.ErrUnauthorized
	}

	return user, nil
}

func (s *AuthService) GetUserByID(ctx context.Context, id int) (*entity.User, error) {
	if id <= 0 {
		return nil, domain.ErrInvalidInput
	}
	user, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, domain.ErrNotFound
	}
	return user, nil
}
