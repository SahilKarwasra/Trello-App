package handler

import (
	"api/cmd/server/services"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type SectionHandler struct {
	sectionService *services.SectionService
}

func NewSectionHandler(sectionService *services.SectionService) *SectionHandler {
	return &SectionHandler{
		sectionService: sectionService,
	}
}

func (h *SectionHandler) CreateSection(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	_, ok := val.(uuid.UUID)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid user id in context"})
		return
	}

	var req services.CreateSectionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	res, err := h.sectionService.CreateSection(c.Request.Context(), req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, res)
}
