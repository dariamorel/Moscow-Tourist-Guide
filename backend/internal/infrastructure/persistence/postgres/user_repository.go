package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain/entity"
	"tour-guide/internal/domain/repository"
)

type userRepo struct {
 	db *pgxpool.Pool
}

func NewUserRepo(db *pgxpool.Pool) repository.UserRepository {
 	return &userRepo{db: db}
}

func (r *userRepo) Create(ctx context.Context, user *entity.User) error {
	q := `
	INSERT INTO users (login, password_hash, created_at)
	VALUES ($1, $2, NOW()) RETURNING id
	`
	return r.db.QueryRow(ctx, q, user.Login, user.PasswordHash).Scan(&user.ID)
}

func (r *userRepo) GetByLogin(ctx context.Context, login string) (*entity.User, error) {
	q := `SELECT id, login, password_hash, created_at FROM users WHERE login = $1`
	var u entity.User
	err := r.db.QueryRow(ctx, q, login).Scan(&u.ID, &u.Login, &u.PasswordHash, &u.CreatedAt)
	if err != nil {
		return nil, mapError(err)
	}
	return &u, nil
}

func (r *userRepo) GetByID(ctx context.Context, id int) (*entity.User, error) {
	q := `SELECT id, login, password_hash, created_at FROM users WHERE id = $1`
	var u entity.User
	err := r.db.QueryRow(ctx, q, id).Scan(&u.ID, &u.Login, &u.PasswordHash, &u.CreatedAt)
	if err != nil {
		return nil, mapError(err)
	}
	return &u, nil
}
