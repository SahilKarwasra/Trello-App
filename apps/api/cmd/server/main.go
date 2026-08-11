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

	// Repositories
	userRepo := repository.NewUserRepository(db)
	orgRepo := repository.NewOrganisationRepository(db)
	boardRepo := repository.NewBoardRepository(db)
	sectionRepo := repository.NewSectionRepository(db)
	issueRepo := repository.NewIssueRepository(db)

	// Services
	authService := services.NewAuthService(userRepo, cfg.JwtSecret)
	orgService := services.NewOrganisationService(orgRepo, userRepo)
	boardService := services.NewBoardService(boardRepo)
	sectionService := services.NewSectionService(sectionRepo)
	issueService := services.NewIssueService(issueRepo)

	// Handlers
	authHandler := handler.NewAuthHandler(authService)
	orgHandler := handler.NewOrganisationHandler(orgService)
	boardHandler := handler.NewBoardHandler(boardService)
	sectionHandler := handler.NewSectionHandler(sectionService)
	issueHandler := handler.NewIssueHandler(issueService)

	// Router
	router := routes.SetupRouter(cfg.JwtSecret, authHandler, orgHandler, boardHandler, sectionHandler, issueHandler)

	log.Println("Starting HTTP server on :8080...")
	if err := router.Run(":8080"); err != nil {
		log.Fatalf("Failed to run server: %v", err)
	}
}
