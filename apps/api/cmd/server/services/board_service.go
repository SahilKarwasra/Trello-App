package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type BoardService struct {
	boardRepo repository.BoardRepository
}

func NewBoardService(boardRepo repository.BoardRepository) *BoardService {
	return &BoardService{
		boardRepo: boardRepo,
	}
}

func (s *BoardService) CreateBoard(ctx context.Context, req BoardRequest) (*BoardResponse, error) {
	board := models.Board{
		ID:           uuid.New(),
		Title:        req.Title,
		Organisation: req.OrganizationID,
	}
	if err := s.boardRepo.CreateBoard(ctx, &board); err != nil {
		return nil, fmt.Errorf("failed to create board: %w", err)
	}
	return &BoardResponse{
		ID:             board.ID,
		Title:          board.Title,
		OrganizationID: board.Organisation,
		CreatedAt:      board.CreatedAt,
		UpdatedAt:      board.UpdatedAt,
	}, nil
}
