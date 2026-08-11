package repository

import (
	"context"
	"database/models"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type SectionRepository interface {
	CreateSection(ctx context.Context, section *models.Section) error
	UpdateSection(ctx context.Context, section *models.Section) error
	FindByID(ctx context.Context, id uuid.UUID) (*models.Section, error)
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

func (r *sectionRepository) FindByID(ctx context.Context, id uuid.UUID) (*models.Section, error) {
	var section models.Section
	err := r.db.WithContext(ctx).Where("id = ?", id).First(&section).Error
	if err != nil {
		return nil, err
	}
	return &section, nil
}
