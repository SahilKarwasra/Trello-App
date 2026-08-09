package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"
)

type OrganisationService struct {
	orgRepo repository.OrganisationRepository
}

func NewOrganisationService(orgRepo repository.OrganisationRepository) *OrganisationService {
	return &OrganisationService{
		orgRepo: orgRepo,
	}
}

func (s *OrganisationService) CreateOrganisation(ctx context.Context, req CreateOrganisationRequest) (*OrganisationResponse, error) {
	org := models.Organisations{
		Title:       req.Title,
		Description: req.Description,
	}

	if err := s.orgRepo.CreateOrganisation(ctx, &org); err != nil {
		return nil, fmt.Errorf("failed to create organisation: %w", err)
	}

	return &OrganisationResponse{
		ID:          org.ID,
		Title:       org.Title,
		Description: org.Description,
		CreatedAt:   org.CreatedAt,
		UpdatedAt:   org.UpdatedAt,
	}, nil
}
