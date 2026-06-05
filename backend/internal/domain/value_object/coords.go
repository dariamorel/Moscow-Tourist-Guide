package valueobject

import "fmt"

type Coordinates struct {
	Lat float64 `json:"lat"`
	Lon float64 `json:"lon"`
}

func NewCoordinates(lat, lon float64) (*Coordinates, error) {
	if lat < -90 || lat > 90 {
		return nil, fmt.Errorf("Invalid latitude: %f", lat)
	}
	if lon < -180 || lon > 180 {
		return nil, fmt.Errorf("Invalid longitude: %f", lon)
	}
	return &Coordinates{Lat: lat, Lon: lon}, nil
}