package entity

import(
	"fmt"
	"time"
	"tour-guide/internal/domain"
)

type User struct {
 	ID           int
	Login        string
	PasswordHash string
	CreatedAt    time.Time
}

func (u *User) Validate() error {
	if u.Login == "" || len(u.Login) < 3 {
		return fmt.Errorf("%w: login must be at least 3 characters", domain.ErrInvalidInput)
	}
	if u.PasswordHash == "" {
		return fmt.Errorf("%w: password hash is required", domain.ErrInvalidInput)
	}
	return nil
}
