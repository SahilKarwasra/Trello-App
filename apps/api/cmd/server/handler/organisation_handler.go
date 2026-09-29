package handler

import (
	"api/cmd/server/services"
	"api/cmd/server/utils"
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
		utils.Unauthorized(c, "unauthorized")
		return
	}

	userID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.CreateOrganisationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.orgService.CreateOrganisation(c.Request.Context(), userID, req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "Organisation created successfully", res)
}

func (h *OrganisationHandler) GetOrganisations(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	userID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	res, err := h.orgService.GetOrganisations(c.Request.Context(), userID)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, "Organisations retrieved successfully", res)
}

func (h *OrganisationHandler) InviteMember(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	inviterID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.InviteMemberRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.orgService.InviteMember(c.Request.Context(), inviterID, req)
	if err != nil {
		utils.BadRequest(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "Member invited successfully", res)
}

func (h *OrganisationHandler) AcceptInvite(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	userID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.AcceptInviteRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.orgService.AcceptInvite(c.Request.Context(), userID, req)
	if err != nil {
		utils.BadRequest(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, "Invite accepted successfully", res)
}
