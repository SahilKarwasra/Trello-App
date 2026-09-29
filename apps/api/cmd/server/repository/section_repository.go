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
	MoveSection(ctx context.Context, sectionID uuid.UUID, newPosition int) (*models.Section, error)
	DeleteSection(ctx context.Context, sectionID uuid.UUID) error
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

func (r *sectionRepository) MoveSection(ctx context.Context, sectionID uuid.UUID, newPosition int) (*models.Section, error) {
	var updatedSection models.Section
	err := r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var section models.Section
		if err := tx.Where("id = ?", sectionID).First(&section).Error; err != nil {
			return fmt.Errorf("section not found: %w", err)
		}

		oldPosition := section.Position
		boardID := section.BoardID

		var count int64
		if err := tx.Model(&models.Section{}).Where("board_id = ?", boardID).Count(&count).Error; err != nil {
			return err
		}

		if newPosition < 1 {
			newPosition = 1
		}
		if newPosition > int(count) {
			newPosition = int(count)
		}

		if oldPosition < newPosition {
			// Moving down: shift items between (oldPosition, newPosition] up by -1
			if err := tx.Model(&models.Section{}).
				Where("board_id = ? AND position > ? AND position <= ?", boardID, oldPosition, newPosition).
				UpdateColumn("position", gorm.Expr("position - ?", 1)).Error; err != nil {
				return err
			}
		} else if oldPosition > newPosition {
			// Moving up: shift items between [newPosition, oldPosition) down by +1
			if err := tx.Model(&models.Section{}).
				Where("board_id = ? AND position >= ? AND position < ?", boardID, newPosition, oldPosition).
				UpdateColumn("position", gorm.Expr("position + ?", 1)).Error; err != nil {
				return err
			}
		}

		section.Position = newPosition
		if err := tx.Save(&section).Error; err != nil {
			return err
		}

		updatedSection = section
		return nil
	})

	if err != nil {
		return nil, err
	}
	return &updatedSection, nil
}

func (r *sectionRepository) DeleteSection(ctx context.Context, sectionID uuid.UUID) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var section models.Section
		if err := tx.Where("id = ?", sectionID).First(&section).Error; err != nil {
			return fmt.Errorf("section not found: %w", err)
		}

		// 1. Delete all issues belonging to this section
		if err := tx.Where("section_id = ?", sectionID.String()).Delete(&models.Issue{}).Error; err != nil {
			return err
		}

		// 2. Delete the section
		if err := tx.Delete(&section).Error; err != nil {
			return err
		}

		// 3. Close the gap of remaining sections on this board
		return tx.Model(&models.Section{}).
			Where("board_id = ? AND position > ?", section.BoardID, section.Position).
			UpdateColumn("position", gorm.Expr("position - ?", 1)).Error
	})
}

