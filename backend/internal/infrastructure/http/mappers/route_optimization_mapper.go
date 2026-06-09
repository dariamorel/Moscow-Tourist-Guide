package mappers

import (
	"tour-guide/internal/application/service"
	"tour-guide/internal/infrastructure/http/api"
)

func ToAPIOptimizeRouteResponse(out *service.OptimizeRouteOutput) api.OptimizeRouteResponse {
	sights := ToAPISights(out.OptimizedOrder)
	
	return api.OptimizeRouteResponse{
		OptimizedOrder: sights,
		TotalTime:      out.TotalTime,
	}
}
