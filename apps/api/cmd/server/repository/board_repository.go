package repository

import (
	"context"
	"database/models"

	"gorm.io/gorm"
)

type BoardRepository interface {
	CreateBoard(ctx context.Context, board *models.Board) error
	GetBoardsByOrganisationID(ctx context.Context, orgID string) ([]models.Board, error)
}

type boardRepository struct {
	db *gorm.DB
}

func NewBoardRepository(db *gorm.DB) BoardRepository {
	return &boardRepository{db: db}
}

func (r *boardRepository) CreateBoard(ctx context.Context, board *models.Board) error {
	return r.db.WithContext(ctx).Create(board).Error
}

func (r *boardRepository) GetBoardsByOrganisationID(ctx context.Context, orgID string) ([]models.Board, error) {
	var boards []models.Board
	err := r.db.WithContext(ctx).Where("organisation = ?", orgID).Find(&boards).Error
	if err != nil {
		return nil, err
	}
	return boards, nil
}
