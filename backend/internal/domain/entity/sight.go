package entity

import(
	"fmt"
	"time"
	"tour-guide/internal/domain"
	"tour-guide/internal/domain/value_object"
)

type Sight struct {
	ID          int64
	Img         string
	Coordinates valueobject.Coordinates
	Tags        []string
	ShortDesc   string
	Description string
	Timetable   string
	Addr        string
	Phone       string
	Subway      string
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

func (s *Sight) Validate() error {
	if s.ID <= 0 {
		return fmt.Errorf("%w: sight id must be positive", domain.ErrInvalidInput)
	}
	if s.Img == "" {
		return fmt.Errorf("%w: image url is required", domain.ErrInvalidInput)
	}
	if s.ShortDesc == "" || s.Description == "" {
		return fmt.Errorf("%w: short description and full description are required", domain.ErrInvalidInput)
	}
	return nil
}
