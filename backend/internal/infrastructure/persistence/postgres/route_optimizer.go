package postgres

import (
	"context"
	"fmt"
	"math"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/domain"
	"tour-guide/internal/domain/entity"
	domainservices "tour-guide/internal/domain/service"
)

type RouteOptimizer struct {
 	db *pgxpool.Pool
}

func NewRouteOptimizer(db *pgxpool.Pool) domainservices.RouteOptimizer {
  	return &RouteOptimizer{db: db}
}

func (o *RouteOptimizer) Optimize(ctx context.Context, sights []*entity.Sight) (*domainservices.OptimizationResult, error) {
	n := len(sights)
	
	if n > 20 {
		return nil, domain.ErrInvalidInput 
	}
	if n <= 2 {
		totalTime, err := o.getTravelTime(ctx, sights[0].ID, sights[1].ID)
		if err != nil {
			return nil, err
		}
		return &domainservices.OptimizationResult{
			OptimizedOrder: sights,
			TotalTime:      time.Duration(totalTime * float64(time.Second)),
		}, nil
	}

	timeMatrix, err := o.buildTimeMatrix(ctx, sights)
	if err != nil {
		return nil, err
	}

	M := n - 2
	INF := math.MaxFloat64

	dp := make([][]float64, 1<<M)
	parent := make([][]int, 1<<M)
	for i := range dp {
		dp[i] = make([]float64, M)
		parent[i] = make([]int, M)
		for j := range dp[i] {
			dp[i][j] = INF
			parent[i][j] = -1
		}
	}

	for i := 0; i < M; i++ {
		dp[1<<i][i] = timeMatrix[0][i+1]
	}

	for mask := 1; mask < (1 << M); mask++ {
		for last := 0; last < M; last++ {
			if (mask & (1 << last)) == 0 || dp[mask][last] == INF {
				continue
			}
			for next := 0; next < M; next++ {
				if (mask & (1 << next)) != 0 {
					continue
				}
				newMask := mask | (1 << next)
				cost := dp[mask][last] + timeMatrix[last+1][next+1]
				if cost < dp[newMask][next] {
					dp[newMask][next] = cost
					parent[newMask][next] = last
				}
			}
		}
	}

	fullMask := (1 << M) - 1
	bestCost := INF
	bestLast := -1

	for i := 0; i < M; i++ {
		cost := dp[fullMask][i] + timeMatrix[i+1][n-1]
		if cost < bestCost {
			bestCost = cost
			bestLast = i
		}
	}

	path := make([]int, n)
	path[0] = 0
	path[n-1] = n - 1

	curr := bestLast
	mask := fullMask
	for step := M - 1; step >= 0; step-- {
		path[step+1] = curr + 1
		prev := parent[mask][curr]
		mask ^= (1 << curr)
		curr = prev
	}

	optimizedSights := make([]*entity.Sight, n)
	for i, idx := range path {
		optimizedSights[i] = sights[idx]
	}

	return &domainservices.OptimizationResult{
		OptimizedOrder: optimizedSights,
		TotalTime:      time.Duration(bestCost * float64(time.Second)),
	}, nil
}

func (o *RouteOptimizer) buildTimeMatrix(ctx context.Context, sights []*entity.Sight) ([][]float64, error) {
	n := len(sights)
	ids := make([]int, n)
	for i, s := range sights {
		ids[i] = s.ID
	}

	query := `
	SELECT sight1_id, sight2_id, EXTRACT(EPOCH FROM travel_time)
	FROM tour_guide.time_between_sights
	WHERE sight1_id = ANY($1) AND sight2_id = ANY($1)
	`
	rows, err := o.db.Query(ctx, query, ids)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	times := make(map[string]float64)
	for rows.Next() {
		var s1, s2 int64
		var seconds float64
		if err := rows.Scan(&s1, &s2, &seconds); err != nil {
			return nil, err
		}
		times[fmt.Sprintf("%d:%d", s1, s2)] = seconds
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	matrix := make([][]float64, n)
	for i := range matrix {
		matrix[i] = make([]float64, n)
		for j := range matrix[i] {
			if i == j {
				matrix[i][j] = 0
			} else {
				t, err := o.getTravelTimeFromCache(times, sights[i].ID, sights[j].ID)
				if err != nil {
				return nil, err
				}
				matrix[i][j] = t
			}
		}
	}
	return matrix, nil
}

func (o *RouteOptimizer) getTravelTimeFromCache(cache map[string]float64, s1, s2 int) (float64, error) {
	key := fmt.Sprintf("%d:%d", s1, s2)
	if t, ok := cache[key]; ok {
		return t, nil
	}
	return 0, fmt.Errorf("%w: no travel time in DB between sight %d and %d", domain.ErrInvalidInput, s1, s2)
}

func (o *RouteOptimizer) getTravelTime(ctx context.Context, s1, s2 int) (float64, error) {
	query := `SELECT EXTRACT(EPOCH FROM travel_time) FROM tour_guide.time_between_sights WHERE sight1_id = $1 AND sight2_id = $2`
	var seconds float64
	err := o.db.QueryRow(ctx, query, s1, s2).Scan(&seconds)
	if err != nil {
		return 0, fmt.Errorf("%w: no travel time in DB between sight %d and %d", domain.ErrInvalidInput, s1, s2)
	}
	return seconds, nil
}

func (o *RouteOptimizer) GetTravelTime(ctx context.Context, sight1ID, sight2ID int) (time.Duration, error) {
	query := `
	SELECT EXTRACT(EPOCH FROM travel_time) 
	FROM tour_guide.time_between_sights 
	WHERE sight1_id = $1 AND sight2_id = $2
	`
	var seconds float64
	err := o.db.QueryRow(ctx, query, sight1ID, sight2ID).Scan(&seconds)
	if err != nil {
		return 0, fmt.Errorf("%w: no travel time between %d and %d", domain.ErrInvalidInput, sight1ID, sight2ID)
	}
	return time.Duration(seconds * float64(time.Second)), nil
}
