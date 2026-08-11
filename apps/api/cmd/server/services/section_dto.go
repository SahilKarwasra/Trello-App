package services

import (
	"time"

	"github.com/google/uuid"
)

type CreateSectionRequest struct {
	Title   string `json:"title" binding:"required"`
	BoardID string `json:"board_id" binding:"required"`
}

type SectionResponse struct {
	ID        uuid.UUID `json:"id"`
	Title     string    `json:"title"`
	BoardID   string    `json:"board_id"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type UpdateSectionRequest struct {
	SectionID string `json:"section_id" binding:"required"`
	Title     string `json:"title" binding:"required"`
}
