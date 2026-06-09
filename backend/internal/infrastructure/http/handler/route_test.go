package handler_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"tour-guide/internal/infrastructure/http/api"
)

func TestRouteEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	user := registerUser(t, env, "routeuser", "SecurePass123!")
	require.NotNil(t, user, "registration failed")
	require.NotNil(t, user.Id, "user id is nil")
	userID := *user.Id

	t.Run("POST /routes - create new route", func(t *testing.T) {
		input := api.RouteInput{
			UserId:       userID,
			PlannedOrder: []int{1, 2, 3},
		}
		reqBody, _ := json.Marshal(input)

		resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusCreated {
			body, _ := io.ReadAll(resp.Body)
			t.Logf("Create route failed: %d - %s", resp.StatusCode, string(body))
		}
		assert.Equal(t, http.StatusCreated, resp.StatusCode)
		
		var route api.Route
		err = json.NewDecoder(resp.Body).Decode(&route)
		require.NoError(t, err)

		assert.Greater(t, route.Id, 0)
		assert.Equal(t, userID, route.UserId)
		assert.NotNil(t, route.PlannedOrder)
		assert.Equal(t, 1, (*route.PlannedOrder)[0].Id)
		assert.Equal(t, 2, (*route.PlannedOrder)[1].Id)
		assert.Equal(t, 3, (*route.PlannedOrder)[2].Id)
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

		url := fmt.Sprintf("%s/routes/%d", env.baseURL, routeID)
		req, _ := http.NewRequest("PUT", url, bytes.NewReader(reqBody))
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
	})

	t.Run("GET /routes - list user routes", func(t *testing.T) {
		_ = createTestRoute(t, env, userID, []int{1, 2})

		url := fmt.Sprintf("%s/routes?user_id=%d", env.baseURL, userID)
		resp, err := env.client.Get(url)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var routes []api.Route
		err = json.NewDecoder(resp.Body).Decode(&routes)
		require.NoError(t, err)
		assert.NotEmpty(t, routes)
	})

	t.Run("DELETE /routes/{id} - success", func(t *testing.T) {
		routeID := createTestRoute(t, env, userID, []int{1, 2})

		url := fmt.Sprintf("%s/routes/%d", env.baseURL, routeID)
		req, _ := http.NewRequest("DELETE", url, nil)
		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusNoContent, resp.StatusCode)

		getResp, err := env.client.Get(fmt.Sprintf("%s/routes/%d", env.baseURL, routeID))
		require.NoError(t, err)
		defer getResp.Body.Close()
		assert.Equal(t, http.StatusNotFound, getResp.StatusCode)
	})

	t.Run("DELETE /routes/{id} - not found", func(t *testing.T) {
		url := fmt.Sprintf("%s/routes/%d", env.baseURL, 99999)
		req, _ := http.NewRequest("DELETE", url, nil)
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
	}
	reqBody, _ := json.Marshal(input)

	resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
	require.NoError(t, err)
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		body, _ := io.ReadAll(resp.Body)
		t.Fatalf("createTestRoute failed: %d - %s", resp.StatusCode, string(body))
	}

	var route api.Route
	err = json.NewDecoder(resp.Body).Decode(&route)
	require.NoError(t, err)
	
	assert.Greater(t, route.Id, 0)
	return route.Id
}

func ptr[T any](v T) *T {
 	return &v
}
