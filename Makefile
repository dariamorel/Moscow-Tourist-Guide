.PHONY: backend-gen backend-build backend-run backend-test backend-clean \
	db-up db-migrate db-down db-reset db-seed \
	up down logs restart \
	fresh e2e e2e-full

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
	cd backend && go test -v -race ./...

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

db-seed:
	@echo "Seeding database with sights data..."
	cd backend && go run ./cmd/seed/main.go
	@echo "Database seeded successfully"

db-reset: db-down db-up db-migrate db-seed
	@echo "Database reset complete (migrated + seeded)"

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

e2e:
	@echo "Running e2e test..."
	@chmod +x backend/e2e-test.sh
	@cd backend && ./e2e-test.sh

e2e-full:
	@echo "Starting full e2e cycle..."
	@echo ""
	@echo "Шаг 1/5: Сброс и сидинг БД..."
	@$(MAKE) db-reset
	@echo ""
	@echo "Шаг 2/5: Сборка бэкенда..."
	@$(MAKE) backend-build
	@echo ""
	@echo "Шаг 3/5: Запуск бэкенда в фоне..."
	@cd backend && ./bin/api & \
	BACKEND_PID=$$!; \
	echo "   PID бэкенда: $$BACKEND_PID"; \
	echo ""; \
	echo "Шаг 4/5: Ожидание запуска сервера..."; \
	for i in 1 2 3 4 5 6 7 8 9 10; do \
		if curl -s http://localhost:8000/health > /dev/null 2>&1; then \
		echo "   Сервер запущен (попытка $$i)"; \
		break; \
	fi; \
	echo "   Ожидание... (попытка $$i)"; \
	sleep 1; \
	done; \
	echo ""; \
	echo "Шаг 5/5: Запуск e2e-теста..."; \
	echo ""; \
	chmod +x backend/e2e-test.sh; \
	cd backend && ./e2e-test.sh; \
	EXIT_CODE=$$?; \
	echo ""; \
	echo "Остановка бэкенда (PID $$BACKEND_PID)..."; \
	kill $$BACKEND_PID 2>/dev/null || true; \
	wait $$BACKEND_PID 2>/dev/null || true; \
	echo "   Бэкенд остановлен"; \
	exit $$EXIT_CODE
