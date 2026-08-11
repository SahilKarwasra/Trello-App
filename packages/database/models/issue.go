package models

import (
	"time"

	"github.com/google/uuid"
)

type Issue struct {
	ID          uuid.UUID `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	Title       string    `gorm:"type:varchar(255);not null" json:"title"`
	Description string    `gorm:"type:text" json:"description"`
	SectionID   string    `gorm:"type:varchar(255);not null" json:"section_id"`
	CreatedBy   string    `gorm:"type:varchar(255);not null" json:"created_by"`
	Position    int       `gorm:"type:integer;not null" json:"position"`
	CreatedAt   time.Time `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt   time.Time `gorm:"autoUpdateTime" json:"updated_at"`
}
