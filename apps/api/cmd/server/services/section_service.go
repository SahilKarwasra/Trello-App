package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type SectionService struct {
	sectionRepo repository.SectionRepository
}

func NewSectionService(sectionRepo repository.SectionRepository) *SectionService {
	return &SectionService{
		sectionRepo: sectionRepo,
	}
}

func (s *SectionService) CreateSection(ctx context.Context, req CreateSectionRequest) (*SectionResponse, error) {
	section := models.Section{
		ID:      uuid.New(),
		Title:   req.Title,
		BoardID: req.BoardID,
	}
	if err := s.sectionRepo.CreateSection(ctx, &section); err != nil {
		return nil, fmt.Errorf("failed to create section: %w", err)
	}
	return &SectionResponse{
		ID:        section.ID,
		Title:     section.Title,
		BoardID:   section.BoardID,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
	}, nil
}

func (s *SectionService) UpdateSection(ctx context.Context, req UpdateSectionRequest) (*SectionResponse, error) {
	sectionID, err := uuid.Parse(req.SectionID)
	if err != nil {
		return nil, fmt.Errorf("invalid section ID: %w", err)
	}

	section, err := s.sectionRepo.FindByID(ctx, sectionID)
	if err != nil {
		return nil, fmt.Errorf("section not found: %w", err)
	}

	section.Title = req.Title

	if err := s.sectionRepo.UpdateSection(ctx, section); err != nil {
		return nil, fmt.Errorf("failed to update section: %w", err)
	}

	return &SectionResponse{
		ID:        section.ID,
		Title:     section.Title,
		BoardID:   section.BoardID,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
	}, nil
}
