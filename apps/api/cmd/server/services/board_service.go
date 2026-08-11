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

func (s *BoardService) CreateBoard(ctx context.Context, creatorID uuid.UUID, req BoardRequest) (*BoardResponse, error) {
	board := models.Board{
		ID:           uuid.New(),
		Title:        req.Title,
		Organisation: req.OrganizationID,
		CreatedBy:    creatorID.String(),
	}
	if err := s.boardRepo.CreateBoard(ctx, &board); err != nil {
		return nil, fmt.Errorf("failed to create board: %w", err)
	}
	return &BoardResponse{
		ID:             board.ID,
		Title:          board.Title,
		OrganizationID: board.Organisation,
		CreatedBy:      board.CreatedBy,
		CreatedAt:      board.CreatedAt,
		UpdatedAt:      board.UpdatedAt,
	}, nil
}

func (s *BoardService) GetBoards(ctx context.Context, req GetBoardsRequest) ([]BoardResponse, error) {
	boards, err := s.boardRepo.GetBoardsByOrganisationID(ctx, req.OrganizationID)
	if err != nil {
		return nil, fmt.Errorf("failed to get boards: %w", err)
	}

	res := make([]BoardResponse, 0, len(boards))
	for _, b := range boards {
		res = append(res, BoardResponse{
			ID:             b.ID,
			Title:          b.Title,
			OrganizationID: b.Organisation,
			CreatedBy:      b.CreatedBy,
			CreatedAt:      b.CreatedAt,
			UpdatedAt:      b.UpdatedAt,
		})
	}

	return res, nil
}
