package mappers

import (
	"tour-guide/internal/domain/entity"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPISight(s *entity.Sight) api.Sight {
	return api.Sight{
		Id:        s.ID,
		Img:       s.Img,
		Lat:       s.Coordinates.Lat,
		Lon:       s.Coordinates.Lon,
		Tags:      &s.Tags,
		ShortDesc: s.ShortDesc,
		Desc:      s.Description,
		Timetable: EmptyStringToNil(s.Timetable),
		Addr:      EmptyStringToNil(s.Addr),
		Phone:     EmptyStringToNil(s.Phone),
		Subway:    EmptyStringToNil(s.Subway),
	}
}

func ToAPISights(sights []*entity.Sight) []api.Sight {
	res := make([]api.Sight, 0, len(sights))
	for _, s := range sights {
		res = append(res, ToAPISight(s))
	}
	return res
}
