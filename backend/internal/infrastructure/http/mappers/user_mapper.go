package mappers

import (
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIUser(u *entity.User) api.User {
	id := u.ID
	login := u.Login
	apiUser := api.User{
		Id:        Ptr(id),
		Login:     Ptr(login),
		CreatedAt: Ptr(u.CreatedAt),
	}

	if !u.CreatedAt.IsZero() {
		apiUser.CreatedAt = Ptr(u.CreatedAt)
	}

	return apiUser
}

func UserInputToEntity(in api.UserInput) *entity.User {
	return &entity.User{
		Login: in.Login,
	}
}
