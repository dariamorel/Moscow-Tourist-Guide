package mappers

import (
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIAchievement(a *entity.Achievement) api.Achievement {
	return api.Achievement{
		Id:            Ptr(a.ID),
		Type:          Ptr(api.AchievementType(a.Type)),
		Name:          Ptr(a.Name),
		Description:   EmptyStringToNil(a.Description),
		IconUrl:       EmptyStringToNil(a.IconURL),
		RequiredCount: Ptr(a.RequiredCount),
		Tag:           EmptyStringToNil(a.Tag),
		Progress:      Ptr(a.Progress),
	}
}

func ToAPIAchievements(list []*entity.Achievement) []api.Achievement {
	res := make([]api.Achievement, 0, len(list))
	for _, a := range list {
		res = append(res, ToAPIAchievement(a))
	}
	return res
}

/*
func UserAchievementInputToEntity(in api.UserAchievementInput) *entity.UserAchievement {
	ua := &entity.UserAchievement{
		UserID:        in.UserId,
		AchievementID: in.AchievementId,
	}
	if in.Progress != nil {
		ua.Progress = int(*in.Progress)
	}
	return ua
}
*/
