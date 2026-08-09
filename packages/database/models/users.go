package models

import "github.com/google/uuid"

type Users struct {
	ID       uuid.UUID `gorm:"type:uuid;primaryKey;default:gen_random_uuid()"`
	Username string    `gorm:"not null"`
	Password string    `gorm:"not null"`
}
