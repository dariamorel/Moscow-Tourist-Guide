package handler_test

import (
	"context"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"

	"tour-guide/internal/infrastructure/http/api"
	"tour-guide/internal/infrastructure/http/handler"
	pg "tour-guide/internal/infrastructure/persistence/postgres"
	"tour-guide/internal/application/service"
)

type testEnv struct {
	t       *testing.T
	db      *pgxpool.Pool
	server  *httptest.Server
	client  *http.Client
	baseURL string
	ctx     context.Context
	cancel  context.CancelFunc
}

func findModuleRoot() (string, error) {
	dir, err := os.Getwd()
	if err != nil {
		return "", err
	}
	
	for {
		if _, err := os.Stat(filepath.Join(dir, "go.mod")); err == nil {
			return dir, nil
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			return "", fmt.Errorf("go.mod not found")
		}
		dir = parent
	}
}

func applyMigrations(t *testing.T, db *pgxpool.Pool) {
	t.Helper()
	
	root, err := findModuleRoot()
	require.NoError(t, err, "failed to find module root")
	
	migrationPath := filepath.Join(root, "migrations", "V1__create_schema.sql")
	
	sqlBytes, err := os.ReadFile(migrationPath)
	require.NoError(t, err, "failed to read migration file: %s", migrationPath)
	
	_, err = db.Exec(context.Background(), string(sqlBytes))
	require.NoError(t, err, "failed to apply migrations")
}

func seedTestData(t *testing.T, db *pgxpool.Pool) {
	t.Helper()
	ctx := context.Background()
	
	_, err := db.Exec(ctx, `
	INSERT INTO tour_guide.sights (id, img, lat, lon, short_desc, full_desc, addr)
	VALUES 
	(1, 'https://example.com/1.jpg', 55.7539, 37.6208, 'Красная площадь', 'Главная площадь', 'Москва'),
	(2, 'https://example.com/2.jpg', 55.7520, 37.6175, 'Кремль', 'Исторический центр', 'Москва'),
	(3, 'https://example.com/3.jpg', 55.7525, 37.6231, 'Храм Василия', 'Православный храм', 'Москва')
	`)
	require.NoError(t, err)

	_, err = db.Exec(ctx, `
	INSERT INTO tour_guide.time_between_sights (sight1_id, sight2_id, travel_time, distance_meters)
	VALUES 
	(1, 2, INTERVAL '15 minutes', 1200), (2, 1, INTERVAL '15 minutes', 1200),
	(2, 3, INTERVAL '20 minutes', 1500), (3, 2, INTERVAL '20 minutes', 1500),
	(1, 3, INTERVAL '10 minutes', 800),  (3, 1, INTERVAL '10 minutes', 800)
	`)
	require.NoError(t, err)

	_, err = db.Exec(ctx, `
	INSERT INTO tour_guide.achievements (id, type, name, description, required_count)
	VALUES 
	(1, 'sights_visited', 'Первые шаги', 'Посетите первую достопримечательность', 1),
	(2, 'routes_completed', 'Путешественник', 'Завершите 3 маршрута', 3)
	`)
	require.NoError(t, err)
}

func setupTestEnv(t *testing.T) *testEnv {
	ctx := context.Background()

	postgresContainer, err := postgres.RunContainer(ctx,
		testcontainers.WithImage("postgres:16-alpine"),
		postgres.WithDatabase("testdb"),
		postgres.WithUsername("test"),
		postgres.WithPassword("test"),
		testcontainers.WithWaitStrategy(
		wait.ForLog("database system is ready to accept connections").
			WithOccurrence(2).
			WithStartupTimeout(30*time.Second)),
	)
	require.NoError(t, err)

	connStr, err := postgresContainer.ConnectionString(ctx, "sslmode=disable")
	require.NoError(t, err)

	db, err := pgxpool.New(ctx, connStr)
	require.NoError(t, err)

	_, err = db.Exec(ctx, "SET search_path TO tour_guide, public;")
 	require.NoError(t, err, "failed to set search_path")
	
	applyMigrations(t, db)
	seedTestData(t, db)

	sightRepo := pg.NewSightRepo(db)
	routeRepo := pg.NewRouteRepo(db)
	userRepo := pg.NewUserRepo(db)
	achievementRepo := pg.NewAchievementRepo(db)
	routeOptimizer := pg.NewRouteOptimizer(db)

	sightService := service.NewSightService(sightRepo)
	achievementService := service.NewAchievementService(achievementRepo, sightRepo, routeRepo)
	routeService := service.NewRouteService(routeRepo, sightRepo, achievementService, routeOptimizer)
	authService := service.NewAuthService(userRepo)
	routeOptimizationService := service.NewRouteOptimizationService(sightRepo, routeOptimizer)

	sightHandler := handler.NewSightHandler(sightService)
	routeHandler := handler.NewRouteHandler(routeService)
	authHandler := handler.NewAuthHandler(authService)
	achievementHandler := handler.NewAchievementHandler(achievementService)
	routeOptimizationHandler := handler.NewRouteOptimizationHandler(routeOptimizationService)

	serverImpl := &apiServerImpl{
		sightHandler:       sightHandler,
		routeHandler:       routeHandler,
		authHandler:        authHandler,
		achievementHandler: achievementHandler,
		routeOptimizationHandler: routeOptimizationHandler,
	}

	r := chi.NewRouter()
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	api.HandlerFromMux(serverImpl, r)

	ts := httptest.NewServer(r)

	return &testEnv{
		t:       t,
		db:      db,
		server:  ts,
		client:  ts.Client(),
		baseURL: ts.URL,
		ctx:     ctx,
		cancel:  func() { _ = postgresContainer.Terminate(ctx) },
	}
}

func (e *testEnv) teardown() {
	e.server.Close()
	e.db.Close()
	e.cancel()
}

type apiServerImpl struct {
	sightHandler             *handler.SightHandler
	routeHandler             *handler.RouteHandler
	authHandler              *handler.AuthHandler
	achievementHandler       *handler.AchievementHandler
	routeOptimizationHandler *handler.RouteOptimizationHandler
}

func (s *apiServerImpl) GetSightsSightId(w http.ResponseWriter, r *http.Request, sightId int) {
 	s.sightHandler.GetSight(w, r, sightId)
}

func (s *apiServerImpl) GetSights(w http.ResponseWriter, r *http.Request, params api.GetSightsParams) {
 	s.sightHandler.GetSights(w, r, params)
}

func (s *apiServerImpl) PostRoutes(w http.ResponseWriter, r *http.Request) {
 	s.routeHandler.CreateRoute(w, r)
}

func (s *apiServerImpl) PutRoutesRouteId(w http.ResponseWriter, r *http.Request, routeId int) {
 	s.routeHandler.UpdateRoute(w, r, routeId)
}

func (s *apiServerImpl) DeleteRoutesRouteId(w http.ResponseWriter, r *http.Request, routeId int) {
 	s.routeHandler.DeleteRoute(w, r, routeId)
}

func (s *apiServerImpl) GetRoutes(w http.ResponseWriter, r *http.Request, params api.GetRoutesParams) {
 	s.routeHandler.GetRoutes(w, r, params)
}

func (s *apiServerImpl) GetRoutesPassed(w http.ResponseWriter, r *http.Request, params api.GetRoutesPassedParams) {
	s.routeHandler.GetRoutesPassed(w, r, params)
}

func (s *apiServerImpl) GetRoutesDeclined(w http.ResponseWriter, r *http.Request, params api.GetRoutesDeclinedParams) {
 	s.routeHandler.GetRoutesDeclined(w, r, params)
}

func (s *apiServerImpl) GetRoutesInProgress(w http.ResponseWriter, r *http.Request, params api.GetRoutesInProgressParams) {
 	s.routeHandler.GetRoutesInProgress(w, r, params)
}

func (s *apiServerImpl) PostUsersRegistration(w http.ResponseWriter, r *http.Request) {
 	s.authHandler.Registration(w, r)
}

func (s *apiServerImpl) PostUsersLogin(w http.ResponseWriter, r *http.Request) {
 	s.authHandler.Login(w, r)
}

func (s *apiServerImpl) GetAchievements(w http.ResponseWriter, r *http.Request, params api.GetAchievementsParams) {
 	s.achievementHandler.GetAchievements(w, r, params)
}

func (s *apiServerImpl) GetAchievementsCompleted(w http.ResponseWriter, r *http.Request, params api.GetAchievementsCompletedParams) {
 	s.achievementHandler.GetAchievementsCompleted(w, r, params)
}

func (s *apiServerImpl) PostRoutesOptimize(w http.ResponseWriter, r *http.Request) {
	s.routeOptimizationHandler.OptimizeRoute(w, r)
}

func (s *apiServerImpl) GetRoutesRouteId(w http.ResponseWriter, r *http.Request, routeId int) {
    s.routeHandler.GetRoute(w, r, routeId)
}
