package entity

import(
	"fmt"
	"time"
	"tour-guide/internal/domain"
)

type RouteStatus string

const (
	StatusPlanned     RouteStatus = "planned"
	StatusInProgress  RouteStatus = "in_progress"
	StatusCompleted   RouteStatus = "completed"
	StatusDeclined    RouteStatus = "declined"
)

type Route struct {
	ID           int
	UserID       int
	Order        []int
	PlannedOrder []int
	TotalTime    string
	Date         time.Time
	Status       RouteStatus
	CreatedAt    time.Time
	UpdatedAt    time.Time
}

// Validate проверяет инварианты маршрута
func (r *Route) Validate() error {
	if r.UserID <= 0 {
		return fmt.Errorf("%w: user id must be positive", domain.ErrInvalidInput)
	}
	if len(r.PlannedOrder) == 0 {
		return fmt.Errorf("%w: planned order must contain at least one sight", domain.ErrInvalidInput)
	}
	if r.Status == "" {
		r.Status = StatusPlanned
	}
	if !isValidRouteStatus(r.Status) {
		return fmt.Errorf("%w: invalid route status", domain.ErrInvalidInput)
	}
	return nil
}

func isValidRouteStatus(s RouteStatus) bool {
	switch s {
	case StatusPlanned, StatusInProgress, StatusCompleted, StatusDeclined:
		return true
	}
	return false
}

func GetRouteStatus(s string) RouteStatus {
	res := RouteStatus(s)
	if isValidRouteStatus(res) {
		return res
	}
	return RouteStatus("")
}
