package application

import "errors"

var (
	ErrValidationFailed = errors.New("validation failed")
	ErrUnauthorized     = errors.New("invalid credentials")
	ErrConflict         = errors.New("resource already exists")
	ErrForbidden        = errors.New("operation not allowed")
)
