package repository

import (
	"context"
	"database/models"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type OrganisationRepository interface {
	CreateOrganisation(ctx context.Context, org *models.Organisations, member *models.Members) error
	FindByID(ctx context.Context, id uuid.UUID) (*models.Organisations, error)
}

type organisationRepository struct {
	db *gorm.DB
}

func NewOrganisationRepository(db *gorm.DB) OrganisationRepository {
	return &organisationRepository{db: db}
}

func (r *organisationRepository) CreateOrganisation(ctx context.Context, org *models.Organisations, member *models.Members) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(org).Error; err != nil {
			return err
		}
		if err := tx.Create(member).Error; err != nil {
			return err
		}
		return nil
	})
}

func (r *organisationRepository) FindByID(ctx context.Context, id uuid.UUID) (*models.Organisations, error) {
	var org models.Organisations
	err := r.db.WithContext(ctx).Where("id = ?", id).First(&org).Error
	if err != nil {
		return nil, err
	}
	return &org, nil
}
