package main

import (
	"api/cmd/server/handler"
	"api/cmd/server/repository"
	"api/cmd/server/routes"
	"api/cmd/server/services"
	"config"
	"database"
	"log"
)

func main() {
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	db, err := database.NewPostgres(cfg.DatabaseUrl)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	log.Println("Connected to database")

	userRepo := repository.NewUserRepository(db)
	authService := services.NewAuthService(userRepo, cfg.JwtSecret)
	authHandler := handler.NewAuthHandler(authService)

	router := routes.SetupRouter(authHandler)

	log.Println("Starting HTTP server on :8080...")
	if err := router.Run(":8080"); err != nil {
		log.Fatalf("Failed to run server: %v", err)
	}
}
