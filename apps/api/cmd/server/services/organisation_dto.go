package services

import (
	"database/models"
	"time"

	"github.com/google/uuid"
)

type CreateOrganisationRequest struct {
	Title       string `json:"title" binding:"required,min=1,max=100"`
	Description string `json:"description" binding:"required"`
}

type OrganisationResponse struct {
	ID          uuid.UUID `json:"id"`
	Title       string    `json:"title"`
	Description string    `json:"description"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type InviteMemberRequest struct {
	Username       string `json:"username" binding:"required"`
	OrganizationID string `json:"organization_id" binding:"required"`
}

type InviteMemberResponse struct {
	ID             uuid.UUID          `json:"id"`
	User           string             `json:"user"`
	OrganizationID string             `json:"organization_id"`
	Role           models.MembersRole `json:"role"`
	Accepted       bool               `json:"accepted"`
	CreatedAt      time.Time          `json:"created_at"`
}
