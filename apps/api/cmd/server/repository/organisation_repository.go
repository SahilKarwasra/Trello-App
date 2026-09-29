package repository

import (
	"context"
	"database/models"

	"gorm.io/gorm"
)

type OrganisationRepository interface {
	CreateOrganisation(ctx context.Context, org *models.Organisations, member *models.Members) error
	CreateInvitation(ctx context.Context, member *models.Members) error
	GetOrganisations(ctx context.Context, userID string) ([]models.Organisations, error)
	FindMember(ctx context.Context, orgID string, userID string) (*models.Members, error)
	UpdateMember(ctx context.Context, member *models.Members) error
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

func (r *organisationRepository) CreateInvitation(ctx context.Context, member *models.Members) error {
	return r.db.WithContext(ctx).Create(member).Error
}

func (r *organisationRepository) FindMember(ctx context.Context, orgID string, userID string) (*models.Members, error) {
	var member models.Members
	err := r.db.WithContext(ctx).Where("organisation = ? AND \"user\" = ?", orgID, userID).First(&member).Error
	if err != nil {
		return nil, err
	}
	return &member, nil
}

func (r *organisationRepository) UpdateMember(ctx context.Context, member *models.Members) error {
	return r.db.WithContext(ctx).Save(member).Error
}

func (r *organisationRepository) GetOrganisations(ctx context.Context, userID string) ([]models.Organisations, error) {
	var orgs []models.Organisations
	err := r.db.WithContext(ctx).
		Table("organisations").
		Joins("JOIN members ON members.organisation = organisations.id::text").
		Where("members.\"user\" = ? AND members.accepted = ?", userID, true).
		Find(&orgs).Error
	if err != nil {
		return nil, err
	}
	return orgs, nil
}

