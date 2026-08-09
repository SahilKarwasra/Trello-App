package models

import "github.com/google/uuid"

type Users struct {
	ID       uuid.UUID `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	Username string    `gorm:"uniqueIndex;not null" json:"username"`
	Password string    `gorm:"not null" json:"-"`
}
