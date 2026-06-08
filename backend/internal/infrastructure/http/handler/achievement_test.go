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

func TestAchievementEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	user := registerUser(t, env, "achieveuser", "Pass123!")
	require.NotNil(t, user)

	t.Run("GET /achievements - list all", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/achievements")
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var achievements []api.Achievement
		err = json.NewDecoder(resp.Body).Decode(&achievements)
		require.NoError(t, err)
		
		assert.NotEmpty(t, achievements)
		assert.Equal(t, "Первые шаги", *achievements[0].Name)
	})

	t.Run("GET /achievements/completed - empty for new user", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/achievements/completed?user_id=" + string(rune(*user.Id)))
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var completed []api.Achievement
		json.NewDecoder(resp.Body).Decode(&completed)
		assert.Empty(t, completed)
	})

	t.Run("PUT /achievements - update progress", func(t *testing.T) {
		input := api.UserAchievementInput{
			UserId:        int(*user.Id),
			AchievementId: 1,
			Progress:      intPtr(1),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", env.baseURL+"/achievements", bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")
		
		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)

		resp2, _ := env.client.Get(env.baseURL + "/achievements/completed?user_id=" + string(rune(*user.Id)))
		var completed []api.Achievement
		json.NewDecoder(resp2.Body).Decode(&completed)
		assert.NotEmpty(t, completed)
		assert.Equal(t, 1, completed[0].Id)
	})

	t.Run("PUT /achievements - invalid progress", func(t *testing.T) {
		input := api.UserAchievementInput{
			UserId:        int(*user.Id),
			AchievementId: 1,
			Progress:      intPtr(-1),
		}
		reqBody, _ := json.Marshal(input)

		req, _ := http.NewRequest("PUT", env.baseURL+"/achievements", bytes.NewReader(reqBody))
		req.Header.Set("Content-Type", "application/json")
		
		resp, err := env.client.Do(req)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusBadRequest, resp.StatusCode)
	})
}

func intPtr(i int) *int {
 	return &i
}
