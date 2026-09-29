package repository

import (
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type SectionRepository interface {
	CreateSection(ctx context.Context, section *models.Section) error
	UpdateSection(ctx context.Context, section *models.Section) error
	GetSections(ctx context.Context, boardID string) ([]models.Section, error)
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

func (r *sectionRepository) GetSections(ctx context.Context, boardID string) ([]models.Section, error) {
	var sections []models.Section
	err := r.db.WithContext(ctx).Where("board_id = ?", boardID).Order("position ASC").Find(&sections).Error
	if err != nil {
		return nil, err
	}
	return sections, nil
}

func (r *sectionRepository) CreateSection(ctx context.Context, section *models.Section) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var count int64
		if err := tx.Model(&models.Section{}).Where("board_id = ?", section.BoardID).Count(&count).Error; err != nil {
			return err
		}

		maxAllowed := int(count) + 1

		if section.Position <= 0 {
			section.Position = maxAllowed
		} else if section.Position > maxAllowed {
			return fmt.Errorf("invalid position %d: position cannot be greater than %d", section.Position, maxAllowed)
		} else {
			err := tx.Model(&models.Section{}).
				Where("board_id = ? AND position >= ?", section.BoardID, section.Position).
				UpdateColumn("position", gorm.Expr("position + ?", 1)).Error
			if err != nil {
				return err
			}
		}

		return tx.Create(section).Error
	})
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
