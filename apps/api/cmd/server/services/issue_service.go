package services

import (
	"api/cmd/server/repository"
	"api/cmd/server/websockets"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type IssueService struct {
	issueRepo   repository.IssueRepository
	sectionRepo repository.SectionRepository
	broadcaster websockets.Broadcaster
}

func NewIssueService(
	issueRepo repository.IssueRepository,
	sectionRepo repository.SectionRepository,
	broadcaster websockets.Broadcaster,
) *IssueService {
	return &IssueService{
		issueRepo:   issueRepo,
		sectionRepo: sectionRepo,
		broadcaster: broadcaster,
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

	res := &IssueResponse{
		ID:          issue.ID,
		Title:       issue.Title,
		Description: issue.Description,
		SectionID:   issue.SectionID,
		CreatedBy:   issue.CreatedBy,
		Position:    issue.Position,
		CreatedAt:   issue.CreatedAt,
		UpdatedAt:   issue.UpdatedAt,
	}

	if s.broadcaster != nil {
		if secUUID, err := uuid.Parse(req.SectionID); err == nil {
			if sec, err := s.sectionRepo.FindByID(ctx, secUUID); err == nil && sec != nil {
				s.broadcaster.Broadcast(sec.BoardID, creatorID, websockets.EventIssueCreated, res)
			}
		}
	}

	return res, nil
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

	res := &IssueResponse{
		ID:          updatedIssue.ID,
		Title:       updatedIssue.Title,
		Description: updatedIssue.Description,
		SectionID:   updatedIssue.SectionID,
		CreatedBy:   updatedIssue.CreatedBy,
		Position:    updatedIssue.Position,
		CreatedAt:   updatedIssue.CreatedAt,
		UpdatedAt:   updatedIssue.UpdatedAt,
	}

	if s.broadcaster != nil {
		if secUUID, err := uuid.Parse(req.NewSectionID); err == nil {
			if sec, err := s.sectionRepo.FindByID(ctx, secUUID); err == nil && sec != nil {
				s.broadcaster.Broadcast(sec.BoardID, userID, websockets.EventIssueMoved, res)
			}
		}
	}

	return res, nil
}

func (s *IssueService) DeleteIssue(ctx context.Context, userID uuid.UUID, req DeleteIssueRequest) error {
	issueUUID, err := uuid.Parse(req.IssueID)
	if err != nil {
		return fmt.Errorf("invalid issue ID: %w", err)
	}

	// Fetch issue to know its section and board before deletion
	issue, err := s.issueRepo.FindByID(ctx, issueUUID)
	if err != nil {
		return fmt.Errorf("issue not found: %w", err)
	}

	var boardID string
	if secUUID, err := uuid.Parse(issue.SectionID); err == nil {
		if sec, err := s.sectionRepo.FindByID(ctx, secUUID); err == nil && sec != nil {
			boardID = sec.BoardID
		}
	}

	if err := s.issueRepo.DeleteIssue(ctx, issueUUID); err != nil {
		return fmt.Errorf("failed to delete issue: %w", err)
	}

	if s.broadcaster != nil && boardID != "" {
		s.broadcaster.Broadcast(boardID, userID, websockets.EventIssueDeleted, map[string]string{
			"issue_id":   req.IssueID,
			"section_id": issue.SectionID,
			"board_id":   boardID,
		})
	}

	return nil
}
