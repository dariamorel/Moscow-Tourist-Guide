package valueobject

import (
	"fmt"
	"regexp"
	"strconv"
	"time"
)

var durationRegex = regexp.MustCompile(`^(\d+)h\s*(\d+)m$`)

func ParseDuration(s string) (time.Duration, error) {
	if s == "" {
		return 0, nil
	}
	matches := durationRegex.FindStringSubmatch(s)
	if matches == nil {
		return 0, fmt.Errorf("invalid duration format %q, expected 'Xh Ym'", s)
	}
	h, _ := strconv.Atoi(matches[1])
	m, _ := strconv.Atoi(matches[2])
	return time.Duration(h)*time.Hour + time.Duration(m)*time.Minute, nil
}

func FormatDuration(d time.Duration) string {
	if d < 0 {
		d = 0
	}
	totalMinutes := int(d.Minutes())
	h := totalMinutes / 60
	m := totalMinutes % 60
	return fmt.Sprintf("%dh %dm", h, m)
}
