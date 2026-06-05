package repository

import (
	"context"
	"tour-guide/internal/domain/entity"
)

type UserRepository interface {
	Create(ctx context.Context, user *entity.User) error
	
	GetByLogin(ctx context.Context, login string) (*entity.User, error)
	
	GetByID(ctx context.Context, id int) (*entity.User, error)
}
