package handler

import (
	"api/cmd/server/services"
	"api/cmd/server/utils"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type IssueHandler struct {
	issueService *services.IssueService
}

func NewIssueHandler(issueService *services.IssueService) *IssueHandler {
	return &IssueHandler{
		issueService: issueService,
	}
}

func (h *IssueHandler) CreateIssue(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	creatorID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.CreateIssueRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.issueService.CreateIssue(c.Request.Context(), creatorID, req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "Issue created successfully", res)
}
