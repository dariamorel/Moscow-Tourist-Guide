package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/jackc/pgx/v5/pgxpool"

	"tour-guide/internal/infrastructure/http/api"
	"tour-guide/internal/infrastructure/http/handler"
	"tour-guide/internal/infrastructure/persistence/postgres"
	"tour-guide/internal/application/service"
)

type config struct {
	ServerPort string
	DBHost     string
	DBPort     string
	DBName     string
	DBUser     string
	DBPassword string
}

func loadConfig() *config {
	return &config{
		ServerPort: getEnv("SERVER_PORT", "8000"),
		DBHost:     getEnv("DB_HOST", "localhost"),
		DBPort:     getEnv("DB_PORT", "5433"),
		DBName:     getEnv("DB_NAME", "tourguide"),
		DBUser:     getEnv("DB_USER", "tourguide"),
		DBPassword: getEnv("DB_PASSWORD", "secret"),
	}
}

func buildDBURL(cfg *config) string {
	return fmt.Sprintf(
		"postgres://%s:%s@%s:%s/%s?sslmode=disable",
		cfg.DBUser, cfg.DBPassword, cfg.DBHost, cfg.DBPort, cfg.DBName,
	)
}

func getEnv(key, defaultValue string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return defaultValue
}

type apiServerImpl struct {
	sightHandler       *handler.SightHandler
	routeHandler       *handler.RouteHandler
	authHandler        *handler.AuthHandler
	achievementHandler *handler.AchievementHandler
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

func main() {
	cfg := loadConfig()

	dbURL := buildDBURL(cfg)
	db, err := pgxpool.New(context.Background(), dbURL)
	if err != nil {
		log.Fatalf("failed to connect to database: %v", err)
	}
	defer db.Close()

	if err := db.Ping(context.Background()); err != nil {
		log.Fatalf("failed to ping database: %v", err)
	}
	log.Println("Connected to PostgreSQL")

	sightRepo := postgres.NewSightRepo(db)
	routeRepo := postgres.NewRouteRepo(db)
	userRepo := postgres.NewUserRepo(db)
	achievementRepo := postgres.NewAchievementRepo(db)
	routeOptimizer := postgres.NewRouteOptimizer(db)

	sightService := service.NewSightService(sightRepo)
	authService := service.NewAuthService(userRepo)
	achievementService := service.NewAchievementService(achievementRepo, sightRepo, routeRepo)
	routeService := service.NewRouteService(routeRepo, sightRepo, achievementService, routeOptimizer)
	routeOptimizationService := service.NewRouteOptimizationService(sightRepo, routeOptimizer)

	sightHandler := handler.NewSightHandler(sightService)
	routeHandler := handler.NewRouteHandler(routeService)
	authHandler := handler.NewAuthHandler(authService)
	achievementHandler := handler.NewAchievementHandler(achievementService)
	routeOptimizationHandler := handler.NewRouteOptimizationHandler(routeOptimizationService)

	serverImpl := &apiServerImpl{
		sightHandler:             sightHandler,
		routeHandler:             routeHandler,
		authHandler:              authHandler,
		achievementHandler:       achievementHandler,
		routeOptimizationHandler: routeOptimizationHandler,
	}

	r := chi.NewRouter()
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	r.Use(middleware.SetHeader("Content-Type", "application/json"))
	r.Use(middleware.AllowContentType("application/json"))

	r.Get("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"ok"}`))
	})

	api.HandlerFromMux(serverImpl, r)

	srv := &http.Server{
		Addr:    ":" + cfg.ServerPort,
		Handler: r,
	}

	go func() {
		log.Printf("Server starting on :%s", cfg.ServerPort)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("HTTP server error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v", err)
	}
	log.Println("Server stopped")
}
