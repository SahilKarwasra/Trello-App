package services

import (
	"time"

	"github.com/google/uuid"
)

type CreateSectionRequest struct {
	Title    string `json:"title" binding:"required"`
	BoardID  string `json:"board_id" binding:"required"`
	Position int    `json:"position"`
}

type GetSectionsRequest struct {
	BoardID string `form:"board_id" json:"board_id" binding:"required"`
}

type SectionResponse struct {
	ID        uuid.UUID `json:"id"`
	Title     string    `json:"title"`
	BoardID   string    `json:"board_id"`
	Position  int       `json:"position"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type SectionWithIssuesResponse struct {
	ID        uuid.UUID       `json:"id"`
	Title     string          `json:"title"`
	BoardID   string          `json:"board_id"`
	Position  int             `json:"position"`
	CreatedAt time.Time       `json:"created_at"`
	UpdatedAt time.Time       `json:"updated_at"`
	Issues    []IssueResponse `json:"issues"`
}

type UpdateSectionRequest struct {
	SectionID string `json:"section_id" binding:"required"`
	Title     string `json:"title" binding:"required"`
}

type MoveSectionRequest struct {
	SectionID   string `json:"section_id" binding:"required"`
	NewPosition int    `json:"new_position" binding:"required,min=1"`
}

type DeleteSectionRequest struct {
	SectionID string `form:"section_id" json:"section_id" binding:"required"`
}

