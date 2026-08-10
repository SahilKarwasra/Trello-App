package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type OrganisationService struct {
	orgRepo repository.OrganisationRepository
}

func NewOrganisationService(orgRepo repository.OrganisationRepository) *OrganisationService {
	return &OrganisationService{
		orgRepo: orgRepo,
	}
}

func (s *OrganisationService) CreateOrganisation(ctx context.Context, creatorID uuid.UUID, req CreateOrganisationRequest) (*OrganisationResponse, error) {
	org := models.Organisations{
		ID:          uuid.New(),
		Title:       req.Title,
		Description: req.Description,
	}

	member := models.Members{
		ID:           uuid.New(),
		User:         creatorID.String(),
		Organisation: org.ID.String(),
		Role:         models.RoleAdmin,
		Accepted:     true,
	}

	if err := s.orgRepo.CreateOrganisation(ctx, &org, &member); err != nil {
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
