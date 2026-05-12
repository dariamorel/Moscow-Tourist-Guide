.PHONY: backend-gen backend-build backend-run backend-test backend-clean \
	db-up db-migrate db-down \
	up down logs restart \
	fresh

backend-gen:
	@echo "Generating HTTP adapters for backend..."
	mkdir -p backend/internal/infrastructure/http/api
	cd backend && go run github.com/oapi-codegen/oapi-codegen/v2/cmd/oapi-codegen@latest \
		-generate types,chi-server,spec \
		-package api \
		openapi.yaml > internal/infrastructure/http/api/server.gen.go
	cd backend && go mod tidy
	cd backend && go get github.com/go-chi/chi/v5 github.com/go-chi/chi/v5/middleware
	@echo "Backend code generation complete"

backend-build:
	@echo "Building backend..."
	cd backend && CGO_ENABLED=0 go build -ldflags="-s -w" -o bin/api ./cmd/api
	@echo "Backend built: backend/bin/api"

backend-run: backend-build
	@echo "Running backend locally..."
	cd backend && ./bin/api

backend-test:
	@echo "Running backend tests..."
	cd backend && go test -v -race -cover ./...

backend-clean:
	@echo "Cleaning backend artifacts..."
	rm -rf backend/bin/ backend/internal/infrastructure/http/api/server.gen.go
	cd backend && go clean -cache -testcache
	@echo "Backend cleaned"

db-up:
	@echo "Starting PostgreSQL..."
	docker-compose up -d postgres

db-migrate:
	@echo "Applying Flyway migrations..."
	docker-compose run --rm flyway migrate

db-down:
	@echo "Removing database volumes..."
	docker-compose down -v

up:
	@echo "Starting all services..."
	docker-compose up --build -d

down:
	@echo "Stopping all services..."
	docker-compose down

logs:
	@echo "Streaming logs (Ctrl+C to stop)..."
	docker-compose logs -f backend frontend

restart: down up
	@echo "Services restarted"

fresh: down backend-clean
	@echo "Removing all volumes and caches..."
	docker volume prune -f
	cd backend && go clean -modcache
	@echo "Project reset complete"
