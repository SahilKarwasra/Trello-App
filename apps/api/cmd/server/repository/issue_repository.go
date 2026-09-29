package repository

import (
	"context"
	"database/models"
	"fmt"

	"gorm.io/gorm"
)

type IssueRepository interface {
	CreateIssue(ctx context.Context, issue *models.Issue) error
	GetIssuesByBoardID(ctx context.Context, boardID string) ([]models.Issue, error)
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
