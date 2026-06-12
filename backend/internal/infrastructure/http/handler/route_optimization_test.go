package handler_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"tour-guide/internal/infrastructure/http/api"
)

func TestRouteOptimizationEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	t.Run("POST /routes/optimize - success (finds shorter path)", func(t *testing.T) {
		_, err := env.db.Exec(env.ctx, `
		INSERT INTO tour_guide.sights (id, img, lat, lon, short_desc, full_desc) 
		VALUES (4, 'img', 0, 0, 't4', 't4')
		`)
		require.NoError(t, err)
		_, err = env.db.Exec(env.ctx, `
		INSERT INTO tour_guide.time_between_sights (sight1_id, sight2_id, travel_time, distance_meters)
		VALUES 
			(1, 4, INTERVAL '15 minutes', 400), (4, 1, INTERVAL '15 minutes', 400),
			(4, 2, INTERVAL '5 minutes', 400), (2, 4, INTERVAL '5 minutes', 400),
			(4, 3, INTERVAL '5 minutes', 400), (3, 4, INTERVAL '5 minutes', 400)
		`)
		require.NoError(t, err)
		
		input := api.OptimizeRouteInput{
			SightIds: []int{1, 4, 2, 3},
		}
		reqBody, _ := json.Marshal(input)

		resp, err := env.client.Post(env.baseURL+"/routes/optimize", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		
		var result api.OptimizeRouteResponse
		err = json.NewDecoder(resp.Body).Decode(&result)
		require.NoError(t, err)

		require.Len(t, result.OptimizedOrder, 4)
		assert.Equal(t, 1, result.OptimizedOrder[0].Id)
		assert.Equal(t, 2, result.OptimizedOrder[1].Id)
		assert.Equal(t, 4, result.OptimizedOrder[2].Id)
		assert.Equal(t, 3, result.OptimizedOrder[3].Id)

		assert.Equal(t, "0h 25m", result.TotalTime)
	})

	t.Run("POST /routes/optimize - error: too many sights (> 20)", func(t *testing.T) {
		ids := make([]int, 25)
		for i := range ids {
			ids[i] = i + 1
		}
		input := api.OptimizeRouteInput{SightIds: ids}
		reqBody, _ := json.Marshal(input)

		resp, err := env.client.Post(env.baseURL+"/routes/optimize", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusBadRequest, resp.StatusCode)
		
		var errResp api.Error
		json.NewDecoder(resp.Body).Decode(&errResp)
		assert.Contains(t, errResp.Message, "Maximum 20 sights")
	})

	t.Run("POST /routes/optimize - error: sight not found (404)", func(t *testing.T) {
		input := api.OptimizeRouteInput{SightIds: []int{1, 9999}}
		reqBody, _ := json.Marshal(input)

		resp, err := env.client.Post(env.baseURL+"/routes/optimize", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusNotFound, resp.StatusCode)
		
		var errResp api.Error
		json.NewDecoder(resp.Body).Decode(&errResp)
		assert.Contains(t, errResp.Message, "not found")
	})
}
