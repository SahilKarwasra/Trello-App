package handler

import (
	"api/cmd/server/services"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type OrganisationHandler struct {
	orgService *services.OrganisationService
}

func NewOrganisationHandler(orgService *services.OrganisationService) *OrganisationHandler {
	return &OrganisationHandler{
		orgService: orgService,
	}
}

func (h *OrganisationHandler) CreateOrganisation(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	userID, ok := val.(uuid.UUID)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid user id in context"})
		return
	}

	var req services.CreateOrganisationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	res, err := h.orgService.CreateOrganisation(c.Request.Context(), userID, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, res)
}
