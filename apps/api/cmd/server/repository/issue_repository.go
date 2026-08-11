package repository

import (
	"context"
	"database/models"
	"fmt"

	"gorm.io/gorm"
)

type IssueRepository interface {
	CreateIssue(ctx context.Context, issue *models.Issue) error
}

type issueRepository struct {
	db *gorm.DB
}

func NewIssueRepository(db *gorm.DB) IssueRepository {
	return &issueRepository{
		db: db,
	}
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
