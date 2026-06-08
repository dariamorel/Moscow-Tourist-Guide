package handler_test

import (
 "encoding/json"
 "net/http"
 "testing"

 "github.com/stretchr/testify/assert"
 "github.com/stretchr/testify/require"

 "tour-guide/internal/infrastructure/http/api"
)

func TestSightsEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	t.Run("GET /sights - empty list with no filters", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/sights")
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		
		var sights []api.Sight
		err = json.NewDecoder(resp.Body).Decode(&sights)
		require.NoError(t, err)
		
		assert.Len(t, sights, 3)
		assert.Equal(t, "Красная площадь", sights[0].ShortDesc)
	})

	t.Run("GET /sights?tag=музей - filter by tag", func(t *testing.T) {
		_, err := env.db.Exec(env.ctx, `
		INSERT INTO tags (name) VALUES ('музей') 
		ON CONFLICT (name) DO NOTHING
		`)
		require.NoError(t, err)
		_, err = env.db.Exec(env.ctx, `
		INSERT INTO sight_to_tag (sight_id, tag_id) 
		SELECT 1, id FROM tags WHERE name = 'музей'
		ON CONFLICT DO NOTHING
		`)
		require.NoError(t, err)

		resp, err := env.client.Get(env.baseURL + "/sights?tag=музей")
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var sights []api.Sight
		json.NewDecoder(resp.Body).Decode(&sights)
		assert.NotEmpty(t, sights)
	})

	t.Run("GET /sights/{id} - found", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/sights/1")
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var sight api.Sight
		err = json.NewDecoder(resp.Body).Decode(&sight)
		require.NoError(t, err)
		
		assert.Equal(t, int64(1), sight.Id)
		assert.Equal(t, "Красная площадь", sight.ShortDesc)
		assert.Equal(t, 55.7539, sight.Lat)
	})

	t.Run("GET /sights/{id} - not found", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/sights/999")
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusNotFound, resp.StatusCode)
		var errResp api.Error
		json.NewDecoder(resp.Body).Decode(&errResp)
		assert.Equal(t, "NOT_FOUND", errResp.Code)
	})

	t.Run("GET /sights/{id} - invalid ID", func(t *testing.T) {
		resp, err := env.client.Get(env.baseURL + "/sights/invalid")
		require.NoError(t, err)
		defer resp.Body.Close()
		assert.Equal(t, http.StatusBadRequest, resp.StatusCode)
	})
}
