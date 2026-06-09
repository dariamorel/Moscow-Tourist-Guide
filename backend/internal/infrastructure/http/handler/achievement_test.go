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

func TestAchievementEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	user := registerUser(t, env, "achieveuser", "SecurePass123!")
	require.NotNil(t, user, "registration failed")
	require.NotNil(t, user.Id, "user id is nil")
	userID := *user.Id

	t.Run("GET /achievements - initial state (zero progress)", func(t *testing.T) {
		url := fmt.Sprintf("%s/achievements?user_id=%d", env.baseURL, userID)
		resp, err := env.client.Get(url)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var achievements []api.Achievement
		err = json.NewDecoder(resp.Body).Decode(&achievements)
		require.NoError(t, err)
		
		var firstSteps *api.Achievement
		for i := range achievements {
			if *achievements[i].Id == 1 {
				firstSteps = &achievements[i]
				break
			}
		}
		require.NotNil(t, firstSteps)
		assert.Equal(t, 0, *firstSteps.Progress, "Initial progress should be 0")
	})

	t.Run("Auto-update achievements on route completion", func(t *testing.T) {
		routeInput := api.RouteInput{
			UserId:       userID,
			PlannedOrder: []int{1, 2},
		}
		reqBody, _ := json.Marshal(routeInput)
		resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		
		var createdRoute api.Route
		err = json.NewDecoder(resp.Body).Decode(&createdRoute)
		require.NoError(t, err)
		resp.Body.Close()

		updateInput := api.RouteUpdateInput{
			Order:  ptr([]int{1, 2}),
			Status: ptr(api.RouteUpdateInputStatusCompleted),
		}
		reqBodyUpdate, _ := json.Marshal(updateInput)
		req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/routes/%d", env.baseURL, createdRoute.Id), bytes.NewReader(reqBodyUpdate))
		req.Header.Set("Content-Type", "application/json")
		
		respPut, err := env.client.Do(req)
		require.NoError(t, err)
		assert.Equal(t, http.StatusOK, respPut.StatusCode)
		respPut.Body.Close()

		url := fmt.Sprintf("%s/achievements?user_id=%d", env.baseURL, userID)
		respCheck, err := env.client.Get(url)
		require.NoError(t, err)
		defer respCheck.Body.Close()

		var updatedAchievements []api.Achievement
		err = json.NewDecoder(respCheck.Body).Decode(&updatedAchievements)
		require.NoError(t, err)

		var firstSteps *api.Achievement
		for i := range updatedAchievements {
			if *updatedAchievements[i].Id == 1 {
				firstSteps = &updatedAchievements[i]
				break
			}
		}
		require.NotNil(t, firstSteps)
		
		assert.Equal(t, 2, *firstSteps.Progress, "Progress should update to 2 after completing route with 2 sights")
	})

	t.Run("Auto-update achievements on route deletion", func(t *testing.T) {
		reqDeleteOld, _ := http.NewRequest("DELETE", fmt.Sprintf("%s/routes/1", env.baseURL), nil)
		respDeleteOld, err := env.client.Do(reqDeleteOld)
		require.NoError(t, err)
		respDeleteOld.Body.Close()

		routeInput := api.RouteInput{
			UserId:       userID,
			PlannedOrder: []int{1, 2},
		}
		reqBody, _ := json.Marshal(routeInput)
		resp, err := env.client.Post(env.baseURL+"/routes", "application/json", bytes.NewReader(reqBody))
		require.NoError(t, err)
		
		var createdRoute api.Route
		err = json.NewDecoder(resp.Body).Decode(&createdRoute)
		require.NoError(t, err)
		resp.Body.Close()

		updateInput := api.RouteUpdateInput{
			Order:  ptr([]int{1, 2}),
			Status: ptr(api.RouteUpdateInputStatusCompleted),
		}
		reqBodyUpdate, _ := json.Marshal(updateInput)
		req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/routes/%d", env.baseURL, createdRoute.Id), bytes.NewReader(reqBodyUpdate))
		req.Header.Set("Content-Type", "application/json")
		
		respPut, err := env.client.Do(req)
		require.NoError(t, err)
		assert.Equal(t, http.StatusOK, respPut.StatusCode)
		respPut.Body.Close()

		url := fmt.Sprintf("%s/achievements?user_id=%d", env.baseURL, userID)
		respCheck, err := env.client.Get(url)
		require.NoError(t, err)
		
		var achievementsBefore []api.Achievement
		err = json.NewDecoder(respCheck.Body).Decode(&achievementsBefore)
		require.NoError(t, err)
		respCheck.Body.Close()

		var firstStepsBefore *api.Achievement
		for i := range achievementsBefore {
			if *achievementsBefore[i].Id == 1 {
				firstStepsBefore = &achievementsBefore[i]
				break
			}
		}
		require.NotNil(t, firstStepsBefore)
		assert.Equal(t, 2, *firstStepsBefore.Progress, "Progress should be 2 after completing route")

		reqDelete, _ := http.NewRequest("DELETE", fmt.Sprintf("%s/routes/%d", env.baseURL, createdRoute.Id), nil)
		respDelete, err := env.client.Do(reqDelete)
		require.NoError(t, err)
		assert.Equal(t, http.StatusNoContent, respDelete.StatusCode)
		respDelete.Body.Close()

		respCheckAfter, err := env.client.Get(url)
		require.NoError(t, err)
		defer respCheckAfter.Body.Close()

		var achievementsAfter []api.Achievement
		err = json.NewDecoder(respCheckAfter.Body).Decode(&achievementsAfter)
		require.NoError(t, err)

		var firstStepsAfter *api.Achievement
		for i := range achievementsAfter {
			if *achievementsAfter[i].Id == 1 {
				firstStepsAfter = &achievementsAfter[i]
				break
			}
		}
		require.NotNil(t, firstStepsAfter)
		assert.Equal(t, 0, *firstStepsAfter.Progress, "Progress should return to 0 after deleting completed route")
	})
}
