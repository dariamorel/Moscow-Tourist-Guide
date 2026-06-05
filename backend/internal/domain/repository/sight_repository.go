package repository

import (
	"context"
	"tour-guide/internal/domain/entity"
)

type SightFilter struct {
	Limit  int
	Offset int
	Tag    *string
}

type SightRepository interface {
	GetByID(ctx context.Context, id int64) (*entity.Sight, error)

	List(ctx context.Context, filter SightFilter) ([]*entity.Sight, error)
}
