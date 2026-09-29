package repository

import (
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type IssueRepository interface {
	CreateIssue(ctx context.Context, issue *models.Issue) error
	GetIssuesByBoardID(ctx context.Context, boardID string) ([]models.Issue, error)
	MoveIssue(ctx context.Context, issueID uuid.UUID, newSectionID string, newPosition int) (*models.Issue, error)
	DeleteIssue(ctx context.Context, issueID uuid.UUID) error
}

type issueRepository struct {
	db *gorm.DB
}

func NewIssueRepository(db *gorm.DB) IssueRepository {
	return &issueRepository{
		db: db,
	}
}

func (r *issueRepository) GetIssuesByBoardID(ctx context.Context, boardID string) ([]models.Issue, error) {
	var issues []models.Issue
	err := r.db.WithContext(ctx).
		Table("issues").
		Joins("JOIN sections ON sections.id::text = issues.section_id").
		Where("sections.board_id = ?", boardID).
		Order("issues.position ASC").
		Find(&issues).Error
	if err != nil {
		return nil, err
	}
	return issues, nil
}

func (r *issueRepository) CreateIssue(ctx context.Context, issue *models.Issue) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var count int64
		if err := tx.Model(&models.Issue{}).Where("section_id = ?", issue.SectionID).Count(&count).Error; err != nil {
			return err
		}

		maxAllowed := int(count) + 1

		if issue.Position <= 0 {
			issue.Position = maxAllowed
		} else if issue.Position > maxAllowed {
			return fmt.Errorf("invalid position %d: position cannot be greater than %d", issue.Position, maxAllowed)
		} else {
			err := tx.Model(&models.Issue{}).
				Where("section_id = ? AND position >= ?", issue.SectionID, issue.Position).
				UpdateColumn("position", gorm.Expr("position + ?", 1)).Error
			if err != nil {
				return err
			}
		}

		return tx.Create(issue).Error
	})
}

func (r *issueRepository) MoveIssue(ctx context.Context, issueID uuid.UUID, newSectionID string, newPosition int) (*models.Issue, error) {
	var updatedIssue models.Issue
	err := r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var issue models.Issue
		if err := tx.Where("id = ?", issueID).First(&issue).Error; err != nil {
			return fmt.Errorf("issue not found: %w", err)
		}

		oldSectionID := issue.SectionID
		oldPosition := issue.Position

		if oldSectionID == newSectionID {
			// Moving within the same section
			var count int64
			if err := tx.Model(&models.Issue{}).Where("section_id = ?", oldSectionID).Count(&count).Error; err != nil {
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
				if err := tx.Model(&models.Issue{}).
					Where("section_id = ? AND position > ? AND position <= ?", oldSectionID, oldPosition, newPosition).
					UpdateColumn("position", gorm.Expr("position - ?", 1)).Error; err != nil {
					return err
				}
			} else if oldPosition > newPosition {
				// Moving up: shift items between [newPosition, oldPosition) down by +1
				if err := tx.Model(&models.Issue{}).
					Where("section_id = ? AND position >= ? AND position < ?", oldSectionID, newPosition, oldPosition).
					UpdateColumn("position", gorm.Expr("position + ?", 1)).Error; err != nil {
					return err
				}
			}

			issue.Position = newPosition
			if err := tx.Save(&issue).Error; err != nil {
				return err
			}
		} else {
			// Moving to a different section
			// 1. Close gap in old section
			if err := tx.Model(&models.Issue{}).
				Where("section_id = ? AND position > ?", oldSectionID, oldPosition).
				UpdateColumn("position", gorm.Expr("position - ?", 1)).Error; err != nil {
				return err
			}

			// 2. Count items in new section
			var newCount int64
			if err := tx.Model(&models.Issue{}).Where("section_id = ?", newSectionID).Count(&newCount).Error; err != nil {
				return err
			}

			if newPosition < 1 {
				newPosition = 1
			}
			maxAllowed := int(newCount) + 1
			if newPosition > maxAllowed {
				newPosition = maxAllowed
			}

			// 3. Make room in new section
			if err := tx.Model(&models.Issue{}).
				Where("section_id = ? AND position >= ?", newSectionID, newPosition).
				UpdateColumn("position", gorm.Expr("position + ?", 1)).Error; err != nil {
				return err
			}

			// 4. Update the issue
			issue.SectionID = newSectionID
			issue.Position = newPosition
			if err := tx.Save(&issue).Error; err != nil {
				return err
			}
		}

		updatedIssue = issue
		return nil
	})

	if err != nil {
		return nil, err
	}
	return &updatedIssue, nil
}

func (r *issueRepository) DeleteIssue(ctx context.Context, issueID uuid.UUID) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var issue models.Issue
		if err := tx.Where("id = ?", issueID).First(&issue).Error; err != nil {
			return fmt.Errorf("issue not found: %w", err)
		}

		if err := tx.Delete(&issue).Error; err != nil {
			return err
		}

		return tx.Model(&models.Issue{}).
			Where("section_id = ? AND position > ?", issue.SectionID, issue.Position).
			UpdateColumn("position", gorm.Expr("position - ?", 1)).Error
	})
}
