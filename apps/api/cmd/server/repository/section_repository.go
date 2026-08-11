package repository

import (
	"context"
	"database/models"

	"gorm.io/gorm"
)

type SectionRepository interface {
	CreateSection(ctx context.Context, section *models.Section) error
	UpdateSection(ctx context.Context, section *models.Section) error
}

type sectionRepository struct {
	db *gorm.DB
}

func NewSectionRepository(db *gorm.DB) SectionRepository {
	return &sectionRepository{
		db: db,
	}
}

func (r *sectionRepository) CreateSection(ctx context.Context, section *models.Section) error {
	return r.db.WithContext(ctx).Create(section).Error
}

func (r *sectionRepository) UpdateSection(ctx context.Context, section *models.Section) error {
	return r.db.WithContext(ctx).Save(section).Error
}
