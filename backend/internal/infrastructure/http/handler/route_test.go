package handler_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"tour-guide/internal/infrastructure/http/api"
)

func ptr[T any](v T) *T {
 	return &v
}

func TestRouteEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	user := registerUser(t, env, "routeuser", "Pass123!")
	require.NotNil(t, user)
	userID := int(*user.Id)

	t.Run("POST /routes - create new route", func(t *testing.T) {
		input := api.RouteInput{
			UserId:       userID,
			PlannedOrder: []int{1, 2, 3},
			TotalTime:    ptr("2h 30m"),
		}
		reqBody, _ := json.Marshal(input)

		resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusCreated, resp.StatusCode)
		var route api.Route
		err = json.NewDecoder(resp.Body).Decode(&route)
		require.NoError(t, err)

		assert.NotNil(t, route.Id)
		assert.Equal(t, userID, route.UserId)
		assert.Equal(t, []int{1, 2, 3}, *route.PlannedOrder)
		assert.Equal(t, api.RouteStatusPlanned, *route.Status)
	})

	t.Run("PUT /routes/{id} - update order and status", func(t *testing.T) {
		routeID := createTestRoute(t, env, userID, []int{1, 2, 3})

		input := api.RouteUpdateInput{
			Order:     ptr([]int{1, 2}),
			TotalTime: ptr("2h 15m"),
			Status:    ptr(api.RouteUpdateInputStatusInProgress),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/routes/%d", env.baseURL, routeID), bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")

		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var updated api.Route
		err = json.NewDecoder(resp.Body).Decode(&updated)
		require.NoError(t, err)

		assert.Equal(t, []int{1, 2}, *updated.Order)
		assert.Equal(t, "2h 15m", *updated.TotalTime)
		assert.Equal(t, api.RouteStatusInProgress, *updated.Status)
		assert.Equal(t, []int{1, 2, 3}, *updated.PlannedOrder)
	})

	t.Run("PUT /routes/{id} - mark as completed", func(t *testing.T) {
		routeID := createTestRoute(t, env, userID, []int{1, 2})

		input := api.RouteUpdateInput{
			Order:  ptr([]int{1, 2}),
			Status: ptr(api.RouteUpdateInputStatusCompleted),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/routes/%d", env.baseURL, routeID), bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")

		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var updated api.Route
		json.NewDecoder(resp.Body).Decode(&updated)

		assert.Equal(t, api.RouteStatusCompleted, *updated.Status)
		assert.Equal(t, []int{1, 2}, *updated.Order)
	})

	t.Run("PUT /routes/{id} - change planned_order", func(t *testing.T) {
		routeID := createTestRoute(t, env, userID, []int{1, 2, 3})

		input := api.RouteUpdateInput{
			PlannedOrder: ptr([]int{3, 1, 2}),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/routes/%d", env.baseURL, routeID), bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")

		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var updated api.Route
		json.NewDecoder(resp.Body).Decode(&updated)

		assert.Equal(t, []int{3, 1, 2}, *updated.PlannedOrder)
		assert.Equal(t, api.RouteStatusPlanned, *updated.Status)
	})

	t.Run("PUT /routes/{id} - not found", func(t *testing.T) {
		input := api.RouteUpdateInput{
			Status: ptr(api.RouteUpdateInputStatusDeclined),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", env.baseURL+"/routes/9999", bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")

		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusNotFound, resp.StatusCode)
	})
}

func createTestRoute(t *testing.T, env *testEnv, userID int, planned []int) int {
	t.Helper()
	input := api.RouteInput{
		UserId:       userID,
		PlannedOrder: planned,
		TotalTime:    ptr("1h"),
	}
	reqBody, _ := json.Marshal(input)

	resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
	require.NoError(t, err)
	defer resp.Body.Close()

	var route api.Route
	_ = json.NewDecoder(resp.Body).Decode(&route)
	return route.Id
}
