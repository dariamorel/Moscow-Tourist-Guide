package valueobject

import (
	"fmt"
	"math"
	"time"
)

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

const earthRadiusKm = 6371.0
const walkingSpeedKmh = 5.0

func (c *Coordinates) DistanceTo(other *Coordinates) float64 {
	lat1Rad := c.Lat * math.Pi / 180
	lat2Rad := other.Lat * math.Pi / 180
	dLat := (other.Lat - c.Lat) * math.Pi / 180
	dLon := (other.Lon - c.Lon) * math.Pi / 180

	a := math.Sin(dLat/2)*math.Sin(dLat/2) +
	math.Cos(lat1Rad)*math.Cos(lat2Rad)*math.Sin(dLon/2)*math.Sin(dLon/2)
	c_val := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1-a))

	return earthRadiusKm * c_val
}

func (c *Coordinates) WalkingTimeTo(other *Coordinates) time.Duration {
	km := c.DistanceTo(other)
	hours := km / walkingSpeedKmh
	return time.Duration(hours * float64(time.Hour))
}
