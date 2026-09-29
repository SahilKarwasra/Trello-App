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
	issueRepo   repository.IssueRepository
}

func NewSectionService(sectionRepo repository.SectionRepository, issueRepo repository.IssueRepository) *SectionService {
	return &SectionService{
		sectionRepo: sectionRepo,
		issueRepo:   issueRepo,
	}
}

func (s *SectionService) GetSections(ctx context.Context, req GetSectionsRequest) ([]SectionResponse, error) {
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

	res := make([]SectionResponse, 0, len(sections))
	for _, sec := range sections {
		secID := sec.ID.String()
		secIssues := issuesBySection[secID]
		if secIssues == nil {
			secIssues = make([]IssueResponse, 0)
		}
		res = append(res, SectionResponse{
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

func (s *SectionService) CreateSection(ctx context.Context, req CreateSectionRequest) (*SectionResponse, error) {
	section := models.Section{
		ID:       uuid.New(),
		Title:    req.Title,
		BoardID:  req.BoardID,
		Position: req.Position,
	}
	if err := s.sectionRepo.CreateSection(ctx, &section); err != nil {
		return nil, fmt.Errorf("failed to create section: %w", err)
	}
	return &SectionResponse{
		ID:        section.ID,
		Title:     section.Title,
		BoardID:   section.BoardID,
		Position:  section.Position,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
		Issues:    make([]IssueResponse, 0),
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
		Position:  section.Position,
		CreatedAt: section.CreatedAt,
		UpdatedAt: section.UpdatedAt,
		Issues:    make([]IssueResponse, 0),
	}, nil
}

