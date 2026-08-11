package routes

import (
	"api/cmd/server/handler"
	"api/cmd/server/middleware"

	"github.com/gin-gonic/gin"
)

func SetupRouter(
	jwtSecret string,
	authHandler *handler.AuthHandler,
	orgHandler *handler.OrganisationHandler,
	boardHandler *handler.BoardHandler,
	sectionHandler *handler.SectionHandler,
) *gin.Engine {
	engine := gin.Default()

	api := engine.Group("/api/v1")
	{
		// Public Auth routes
		auth := api.Group("/auth")
		{
			auth.POST("/sign-up", authHandler.SignUp)
			auth.POST("/sign-in", authHandler.SignIn)
		}

		// Protected routes (Requires Bearer JWT token)
		protected := api.Group("")
		protected.Use(middleware.AuthMiddleware(jwtSecret))
		{
			protected.POST("/organisation", orgHandler.CreateOrganisation)
			protected.POST("/board", boardHandler.CreateBoard)
			protected.GET("/board", boardHandler.GetBoards)
			protected.POST("/invite", orgHandler.InviteMember)
			protected.PATCH("/accept-invite", orgHandler.AcceptInvite)
			protected.POST("/section", sectionHandler.CreateSection)
		}
	}

	return engine
}
