run:
	go run apps/api/cmd/server/main.go

migrate:
	go run apps/api/cmd/migrate/main.go


tidy:
	cd packages/config && go mod tidy
	cd packages/database && go mod tidy
	cd apps/api && go mod tidy
	go work sync

build:
	go build -o bin/server apps/api/cmd/server/main.go
	go build -o bin/migrate apps/api/cmd/migrate/main.go