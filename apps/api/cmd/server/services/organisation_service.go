package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type OrganisationService struct {
	orgRepo  repository.OrganisationRepository
	userRepo repository.UserRepository
}

func NewOrganisationService(orgRepo repository.OrganisationRepository, userRepo repository.UserRepository) *OrganisationService {
	return &OrganisationService{
		orgRepo:  orgRepo,
		userRepo: userRepo,
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

func (s *OrganisationService) InviteMember(ctx context.Context, inviterID uuid.UUID, req InviteMemberRequest) (*InviteMemberResponse, error) {
	// 1. Verify target user exists
	targetUser, err := s.userRepo.FindByUsername(ctx, req.Username)
	if err != nil {
		return nil, fmt.Errorf("user %q not found", req.Username)
	}

	// 2. Verify inviter is a member of the organisation
	_, err = s.orgRepo.FindMember(ctx, req.OrganizationID, inviterID.String())
	if err != nil {
		return nil, fmt.Errorf("unauthorized: you are not a member of this organisation")
	}

	// 3. Check if target user is already invited or a member
	existing, _ := s.orgRepo.FindMember(ctx, req.OrganizationID, targetUser.ID.String())
	if existing != nil {
		return nil, fmt.Errorf("user %q is already a member or invited to this organisation", req.Username)
	}

	// 4. Create invitation record
	member := models.Members{
		ID:           uuid.New(),
		User:         targetUser.ID.String(),
		Organisation: req.OrganizationID,
		Role:         models.RoleMember,
		Accepted:     false,
	}

	if err := s.orgRepo.CreateInvitation(ctx, &member); err != nil {
		return nil, fmt.Errorf("failed to invite member: %w", err)
	}

	return &InviteMemberResponse{
		ID:             member.ID,
		User:           targetUser.Username,
		OrganizationID: member.Organisation,
		Role:           member.Role,
		Accepted:       member.Accepted,
		CreatedAt:      member.CreatedAt,
	}, nil
}

func (s *OrganisationService) AcceptInvite(ctx context.Context, userID uuid.UUID, req AcceptInviteRequest) (*AcceptInviteResponse, error) {
	// 1. check if invitation Exist
	member, err := s.orgRepo.FindMember(ctx, req.OrganizationID, userID.String())
	if err != nil {
		return nil, fmt.Errorf("no invitation found for this organisation")
	}

	// 2. Check if invitation already accepted
	if member.Accepted {
		return nil, fmt.Errorf("invitation has already been accepted")
	}

	// 3. accept invitation
	member.Accepted = true
	if err := s.orgRepo.UpdateMember(ctx, member); err != nil {
		return nil, fmt.Errorf("failed to accept invite: %w", err)
	}

	return &AcceptInviteResponse{
		Message: "Invite accepted successfully",
	}, nil
}
