package services

import (
	"time"

	"github.com/google/uuid"
)

type CreateIssueRequest struct {
	Title       string `json:"title" binding:"required,min=1,max=255"`
	Description string `json:"description"`
	SectionID   string `json:"section_id" binding:"required"`
	Position    int    `json:"position"`
}

type IssueResponse struct {
	ID          uuid.UUID `json:"id"`
	Title       string    `json:"title"`
	Description string    `json:"description"`
	SectionID   string    `json:"section_id"`
	CreatedBy   string    `json:"created_by"`
	Position    int       `json:"position"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}
