package service

import (
	"context"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type SightFilter struct {
	Limit  int
	Offset int
	Tag    *string
}

type SightService struct {
 	repo repository.SightRepository
}

func NewSightService(repo repository.SightRepository) *SightService {
 	return &SightService{repo: repo}
}

func (s *SightService) CreateSight(ctx context.Context, sight *entity.Sight) error {
	if err := sight.Validate(); err != nil {
		return err
	}
	return s.repo.Create(ctx, sight)
}

func (s *SightService) GetSight(ctx context.Context, id int) (*entity.Sight, error) {
	if id <= 0 {
		return nil, domain.ErrInvalidInput
	}

	sight, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if sight == nil {
		return nil, domain.ErrNotFound
	}
	return sight, nil
}

func (s *SightService) ListSights(ctx context.Context, f SightFilter) ([]*entity.Sight, error) {
	if f.Limit <= 0 {
		f.Limit = 20
	}
	if f.Limit > 100 {
		f.Limit = 100
	}
	if f.Offset < 0 {
		f.Offset = 0
	}

	return s.repo.List(ctx, repository.SightFilter{
		Limit:  f.Limit,
		Offset: f.Offset,
		Tag:    f.Tag,
	})
}
