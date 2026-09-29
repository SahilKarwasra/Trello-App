package handler

import (
	"api/cmd/server/services"
	"api/cmd/server/utils"
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
		utils.Unauthorized(c, "unauthorized")
		return
	}

	_, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.CreateSectionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.sectionService.CreateSection(c.Request.Context(), req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "Section created successfully", res)
}

func (h *SectionHandler) UpdateSection(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	_, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.UpdateSectionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.sectionService.UpdateSection(c.Request.Context(), req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, "Section updated successfully", res)
}

