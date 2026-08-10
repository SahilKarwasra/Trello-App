package repository

import (
	"context"
	"database/models"

	"gorm.io/gorm"
)

type BoardRepository interface {
	CreateBoard(ctx context.Context, board *models.Board) error
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
