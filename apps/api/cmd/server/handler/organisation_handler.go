package handler

import (
	"api/cmd/server/services"
	"net/http"

	"github.com/gin-gonic/gin"
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
	var req services.CreateOrganisationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	res, err := h.orgService.CreateOrganisation(c.Request.Context(), req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, res)
}
