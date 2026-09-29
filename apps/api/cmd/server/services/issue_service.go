package services

import (
	"api/cmd/server/repository"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type IssueService struct {
	issueRepo repository.IssueRepository
}

func NewIssueService(issueRepo repository.IssueRepository) *IssueService {
	return &IssueService{
		issueRepo: issueRepo,
	}
}

func (s *IssueService) CreateIssue(ctx context.Context, creatorID uuid.UUID, req CreateIssueRequest) (*IssueResponse, error) {
	issue := models.Issue{
		ID:          uuid.New(),
		Title:       req.Title,
		Description: req.Description,
		SectionID:   req.SectionID,
		CreatedBy:   creatorID.String(),
		Position:    req.Position,
	}

	if err := s.issueRepo.CreateIssue(ctx, &issue); err != nil {
		return nil, fmt.Errorf("failed to create issue: %w", err)
	}

	return &IssueResponse{
		ID:          issue.ID,
		Title:       issue.Title,
		Description: issue.Description,
		SectionID:   issue.SectionID,
		CreatedBy:   issue.CreatedBy,
		Position:    issue.Position,
		CreatedAt:   issue.CreatedAt,
		UpdatedAt:   issue.UpdatedAt,
	}, nil
}

func (s *IssueService) MoveIssue(ctx context.Context, userID uuid.UUID, req MoveIssueRequest) (*IssueResponse, error) {
	issueUUID, err := uuid.Parse(req.IssueID)
	if err != nil {
		return nil, fmt.Errorf("invalid issue ID: %w", err)
	}

	updatedIssue, err := s.issueRepo.MoveIssue(ctx, issueUUID, req.NewSectionID, req.NewPosition)
	if err != nil {
		return nil, fmt.Errorf("failed to move issue: %w", err)
	}

	return &IssueResponse{
		ID:          updatedIssue.ID,
		Title:       updatedIssue.Title,
		Description: updatedIssue.Description,
		SectionID:   updatedIssue.SectionID,
		CreatedBy:   updatedIssue.CreatedBy,
		Position:    updatedIssue.Position,
		CreatedAt:   updatedIssue.CreatedAt,
		UpdatedAt:   updatedIssue.UpdatedAt,
	}, nil
}

func (s *IssueService) DeleteIssue(ctx context.Context, req DeleteIssueRequest) error {
	issueUUID, err := uuid.Parse(req.IssueID)
	if err != nil {
		return fmt.Errorf("invalid issue ID: %w", err)
	}

	if err := s.issueRepo.DeleteIssue(ctx, issueUUID); err != nil {
		return fmt.Errorf("failed to delete issue: %w", err)
	}

	return nil
}

