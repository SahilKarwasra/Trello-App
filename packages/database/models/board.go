package models

import (
	"time"

	"github.com/google/uuid"
)

type Board struct {
	ID           uuid.UUID `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	Title        string    `gorm:"type:varchar(255);not null" json:"title"`
	Organisation string    `gorm:"type:varchar(255);not null" json:"organisation"`
	CreatedBy    string    `gorm:"type:varchar(255);not null;default:''" json:"created_by"`
	CreatedAt    time.Time `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt    time.Time `gorm:"autoUpdateTime" json:"updated_at"`
}
