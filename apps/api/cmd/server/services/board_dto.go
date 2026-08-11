package services

import (
	"time"

	"github.com/google/uuid"
)

type BoardRequest struct {
	Title          string `json:"title" binding:"required,min=1,max=100"`
	OrganizationID string `json:"organization_id" binding:"required"`
}

type BoardResponse struct {
	ID             uuid.UUID `json:"id"`
	Title          string    `json:"title"`
	OrganizationID string    `json:"organization_id"`
	CreatedBy      string    `json:"created_by"`
	CreatedAt      time.Time `json:"created_at"`
	UpdatedAt      time.Time `json:"updated_at"`
}

type GetBoardsRequest struct {
	OrganizationID string `form:"organization_id" binding:"required"`
}
