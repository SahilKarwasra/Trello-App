package models

import (
	"time"

	"github.com/google/uuid"
)

type Section struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	Title     string    `gorm:"type:text;not null" json:"title"`
	BoardID   string    `gorm:"type:varchar(255);not null" json:"board_id"`
	Position  int       `gorm:"type:integer;not null" json:"position"`
	CreatedAt time.Time `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt time.Time `gorm:"autoUpdateTime" json:"updated_at"`
}
