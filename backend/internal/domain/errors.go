package domain

import "errors"

var (
	ErrNotFound      = errors.New("entity not found")
	ErrAlreadyExists = errors.New("entity already exists")
	ErrInvalidInput  = errors.New("invalid input data")
	ErrUnauthorized  = errors.New("unauthorized access")
	ErrForbidden     = errors.New("action forbidden")
)
