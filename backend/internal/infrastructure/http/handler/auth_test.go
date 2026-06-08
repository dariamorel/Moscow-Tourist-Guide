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

func TestAuthEndpoints(t *testing.T) {
	env := setupTestEnv(t)
	defer env.teardown()

	t.Run("POST /users/registration - success", func(t *testing.T) {
		body := api.UserInput{Login: "testuser", Password: "SecurePass123!"}
		reqBody, _ := json.Marshal(body)

		resp, err := env.client.Post(
			env.baseURL+"/users/registration",
			"application/json",
			bytes.NewReader(reqBody),
		)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusCreated, resp.StatusCode)
		var user api.User
		err = json.NewDecoder(resp.Body).Decode(&user)
		require.NoError(t, err)

		require.NotNil(t, user.Login, "login should not be nil")
		assert.Equal(t, "testuser", *user.Login)

		assert.NotNil(t, user.Id)
		assert.Greater(t, *user.Id, 0)

		if user.CreatedAt != nil {
			assert.False(t, user.CreatedAt.IsZero(), "created_at should not be zero")
		}
	})

	t.Run("POST /users/registration - duplicate login", func(t *testing.T) {
		_ = registerUser(t, env, "dupuser", "Pass123!")

		body := api.UserInput{Login: "dupuser", Password: "AnotherPass!"}
		reqBody, _ := json.Marshal(body)

		resp, err := env.client.Post(
			env.baseURL+"/users/registration",
			"application/json",
			bytes.NewReader(reqBody),
		)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusConflict, resp.StatusCode)
	})

	t.Run("POST /users/login - success", func(t *testing.T) {
		login := "loginuser"
		pass := "MySecurePass!"
		_ = registerUser(t, env, login, pass)

		body := api.UserInput{Login: login, Password: pass}
		reqBody, _ := json.Marshal(body)

		resp, err := env.client.Post(
			env.baseURL+"/users/login",
			"application/json",
			bytes.NewReader(reqBody),
		)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode)
		var result map[string]any
		err = json.NewDecoder(resp.Body).Decode(&result)
		require.NoError(t, err)
		
		assert.Contains(t, result, "token")
		assert.Contains(t, result, "user")
	})

	t.Run("POST /users/login - wrong password", func(t *testing.T) {
		login := "wrongpassuser"
		_ = registerUser(t, env, login, "CorrectPass!")

		body := api.UserInput{Login: login, Password: "WrongPass!"}
		reqBody, _ := json.Marshal(body)

		resp, err := env.client.Post(
			env.baseURL+"/users/login",
			"application/json",
			bytes.NewReader(reqBody),
		)
		require.NoError(t, err)
		defer resp.Body.Close()

		assert.Equal(t, http.StatusUnauthorized, resp.StatusCode)
	})
}

func registerUser(t *testing.T, env *testEnv, login, password string) *api.User {
	t.Helper()
	body := api.UserInput{Login: login, Password: password}
	reqBody, _ := json.Marshal(body)

	resp, err := env.client.Post(
		env.baseURL+"/users/registration",
		"application/json",
		bytes.NewReader(reqBody),
	)
	require.NoError(t, err)
	defer resp.Body.Close()

	var user api.User
	if resp.StatusCode == http.StatusCreated {
		_ = json.NewDecoder(resp.Body).Decode(&user)
	}
	return &user
}
