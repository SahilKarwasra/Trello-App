package models

import (
	"time"

	"github.com/google/uuid"
)

type MembersRole string

const (
	RoleAdmin  MembersRole = "admin"
	RoleMember MembersRole = "member"
)

type Members struct {
	ID           uuid.UUID   `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	User         string      `gorm:"type:varchar(255);not null" json:"user"`
	Organisation string      `gorm:"type:varchar(255);not null" json:"organisation"`
	Role         MembersRole `gorm:"type:varchar(50);not null" json:"role"`
	Accepted     bool        `gorm:"type:boolean;not null;default:false" json:"accepted"`
	CreatedAt    time.Time   `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt    time.Time   `gorm:"autoUpdateTime" json:"updated_at"`
}
