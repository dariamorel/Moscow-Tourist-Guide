package mappers

func Ptr[T any](v T) *T { return &v }

func Val[T any](p *T, defaultValue T) T {
	if p == nil {
		return defaultValue
	}
	return *p
}

func EmptyStringToNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
