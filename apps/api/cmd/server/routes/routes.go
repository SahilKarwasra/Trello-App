package routes

import (
	"api/cmd/server/handler"
	"api/cmd/server/middleware"
	"api/cmd/server/utils"
	"net/http"

	"github.com/gin-gonic/gin"
)

func SetupRouter(
	jwtSecret string,
	authHandler *handler.AuthHandler,
	orgHandler *handler.OrganisationHandler,
	boardHandler *handler.BoardHandler,
	sectionHandler *handler.SectionHandler,
	issueHandler *handler.IssueHandler,
) *gin.Engine {
	engine := gin.Default()

	engine.NoRoute(func(c *gin.Context) {
		utils.NotFound(c, "route not found")
	})

	engine.NoMethod(func(c *gin.Context) {
		utils.Error(c, http.StatusMethodNotAllowed, "method not allowed")
	})

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
			protected.GET("/organisation", orgHandler.GetOrganisations)
			protected.POST("/board", boardHandler.CreateBoard)
			protected.GET("/board", boardHandler.GetBoards)
			protected.POST("/invite", orgHandler.InviteMember)
			protected.PATCH("/accept-invite", orgHandler.AcceptInvite)
			protected.POST("/section", sectionHandler.CreateSection)
			protected.PUT("/section", sectionHandler.UpdateSection)
			protected.POST("/issue", issueHandler.CreateIssue)
		}
	}

	return engine
}
