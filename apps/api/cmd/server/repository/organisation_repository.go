package repository

import (
	"context"
	"database/models"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type OrganisationRepository interface {
	CreateOrganisation(ctx context.Context, org *models.Organisations) error
	FindByID(ctx context.Context, id uuid.UUID) (*models.Organisations, error)
}

type organisationRepository struct {
	db *gorm.DB
}

func NewOrganisationRepository(db *gorm.DB) OrganisationRepository {
	return &organisationRepository{db: db}
}

func (r *organisationRepository) CreateOrganisation(ctx context.Context, org *models.Organisations) error {
	return r.db.WithContext(ctx).Create(org).Error
}

func (r *organisationRepository) FindByID(ctx context.Context, id uuid.UUID) (*models.Organisations, error) {
	var org models.Organisations
	err := r.db.WithContext(ctx).Where("id = ?", id).First(&org).Error
	if err != nil {
		return nil, err
	}
	return &org, nil
}
