package mappers

import (
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIUser(u *entity.User) api.User {
	id := u.ID
	login := u.Login
	return api.User{
		Id:        &id,
		Login:     &login,
		CreatedAt: Ptr(u.CreatedAt),
	}
}

func UserInputToEntity(in api.UserInput) *entity.User {
	return &entity.User{
		Login: in.Login,
	}
}
