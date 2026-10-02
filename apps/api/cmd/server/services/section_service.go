package services

import (
	"api/cmd/server/repository"
	"api/cmd/server/websockets"
	"context"
	"database/models"
	"fmt"

	"github.com/google/uuid"
)

type SectionService struct {
	sectionRepo repository.SectionRepository
	issueRepo   repository.IssueRepository
	broadcaster websockets.Broadcaster
}

func NewSectionService(
	sectionRepo repository.SectionRepository,
	issueRepo repository.IssueRepository,
	broadcaster websockets.Broadcaster,
) *SectionService {
	return &SectionService{
		sectionRepo: sectionRepo,
		issueRepo:   issueRepo,
		broadcaster: broadcaster,
	}
}

func (s *SectionService) GetSections(ctx context.Context, req GetSectionsRequest) ([]SectionWithIssuesResponse, error) {
	sections, err := s.sectionRepo.GetSections(ctx, req.BoardID)
	if err != nil {
		return nil, fmt.Errorf("failed to get sections: %w", err)
	}

	issues, err := s.issueRepo.GetIssuesByBoardID(ctx, req.BoardID)
	if err != nil {
		return nil, fmt.Errorf("failed to get issues: %w", err)
	}

	issuesBySection := make(map[string][]IssueResponse)
	for _, issue := range issues {
		issuesBySection[issue.SectionID] = append(issuesBySection[issue.SectionID], IssueResponse{
			ID:          issue.ID,
			Title:       issue.Title,
			Description: issue.Description,
			SectionID:   issue.SectionID,
			CreatedBy:   issue.CreatedBy,
			Position:    issue.Position,
			CreatedAt:   issue.CreatedAt,
			UpdatedAt:   issue.UpdatedAt,
		})
	}

	res := make([]SectionWithIssuesResponse, 0, len(sections))
	for _, sec := range sections {
		secID := sec.ID.String()
		secIssues := issuesBySection[secID]
		if secIssues == nil {
			secIssues = make([]IssueResponse, 0)
		}
		res = append(res, SectionWithIssuesResponse{
			ID:        sec.ID,
			Title:     sec.Title,
			BoardID:   sec.BoardID,
			Position:  sec.Position,
			CreatedAt: sec.CreatedAt,
			UpdatedAt: sec.UpdatedAt,
			Issues:    secIssues,
		})
	}

	return res, nil
}

func (s *SectionService) CreateSection(ctx context.Context, actorID uuid.UUID, req CreateSectionRequest) (*SectionResponse, error) {
	section := models.Section{
		ID:       uuid.New(),
		Title:    req.Title,
		BoardID:  req.BoardID,
		Position: req.Position,
	}
	if err := s.sectionRepo.CreateSection(ctx, &section); err != nil {
		return nil, fmt.Errorf("failed to create section: %w", err)
	}

	res := &SectionResponse{
		ID:        section.ID,
		Title:     section.Title,
		BoardID:   section.BoardID,
		Position:  section.Position,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
	}

	if s.broadcaster != nil {
		s.broadcaster.Broadcast(res.BoardID, actorID, websockets.EventSectionCreated, res)
	}

	return res, nil
}

func (s *SectionService) UpdateSection(ctx context.Context, actorID uuid.UUID, req UpdateSectionRequest) (*SectionResponse, error) {
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

	res := &SectionResponse{
		ID:        section.ID,
		Title:     section.Title,
		BoardID:   section.BoardID,
		Position:  section.Position,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
	}

	if s.broadcaster != nil {
		s.broadcaster.Broadcast(res.BoardID, actorID, websockets.EventSectionUpdated, res)
	}

	return res, nil
}

func (s *SectionService) MoveSection(ctx context.Context, actorID uuid.UUID, req MoveSectionRequest) (*SectionResponse, error) {
	sectionUUID, err := uuid.Parse(req.SectionID)
	if err != nil {
		return nil, fmt.Errorf("invalid section ID: %w", err)
	}

	updatedSection, err := s.sectionRepo.MoveSection(ctx, sectionUUID, req.NewPosition)
	if err != nil {
		return nil, fmt.Errorf("failed to move section: %w", err)
	}

	res := &SectionResponse{
		ID:        updatedSection.ID,
		Title:     updatedSection.Title,
		BoardID:   updatedSection.BoardID,
		Position:  updatedSection.Position,
		CreatedAt: updatedSection.CreatedAt,
		UpdatedAt: updatedSection.UpdatedAt,
	}

	if s.broadcaster != nil {
		s.broadcaster.Broadcast(res.BoardID, actorID, websockets.EventSectionMoved, res)
	}

	return res, nil
}

func (s *SectionService) DeleteSection(ctx context.Context, actorID uuid.UUID, req DeleteSectionRequest) error {
	sectionUUID, err := uuid.Parse(req.SectionID)
	if err != nil {
		return fmt.Errorf("invalid section ID: %w", err)
	}

	section, err := s.sectionRepo.FindByID(ctx, sectionUUID)
	if err != nil {
		return fmt.Errorf("section not found: %w", err)
	}

	boardID := section.BoardID

	if err := s.sectionRepo.DeleteSection(ctx, sectionUUID); err != nil {
		return fmt.Errorf("failed to delete section: %w", err)
	}

	if s.broadcaster != nil {
		s.broadcaster.Broadcast(boardID, actorID, websockets.EventSectionDeleted, map[string]string{
			"section_id": req.SectionID,
			"board_id":   boardID,
		})
	}

	return nil
}
