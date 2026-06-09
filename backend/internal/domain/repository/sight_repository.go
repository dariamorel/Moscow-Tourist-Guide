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
	GetByID(ctx context.Context, id int) (*entity.Sight, error)
	List(ctx context.Context, filter SightFilter) ([]*entity.Sight, error)
	 Create(ctx context.Context, sight *entity.Sight) error
 	GetAllBasic(ctx context.Context) ([]struct{ ID int; Lat, Lon float64 }, error)
}
