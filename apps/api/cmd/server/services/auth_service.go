package services

import (
	"api/cmd/server/repository"
	"api/cmd/server/utils"
	"context"
	"database/models"
	"errors"
	"fmt"
	"time"

	"gorm.io/gorm"
)

type AuthService struct {
	userRepo  repository.UserRepository
	jwtSecret string
}

func NewAuthService(userRepo repository.UserRepository, jwtSecret string) *AuthService {
	return &AuthService{
		userRepo:  userRepo,
		jwtSecret: jwtSecret,
	}
}

func (s *AuthService) SignUp(ctx context.Context, req SignUpRequest) (*AuthResponse, error) {
	existing, err := s.userRepo.FindByUsername(ctx, req.Username)
	if err == nil && existing != nil {
		return nil, utils.ErrUsernameAlreadyExists
	} else if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, fmt.Errorf("failed to query database: %w", err)
	}

	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		return nil, fmt.Errorf("failed to hash password: %w", err)
	}

	user := models.Users{
		Username: req.Username,
		Password: hashedPassword,
	}

	if err := s.userRepo.CreateUser(ctx, &user); err != nil {
		return nil, fmt.Errorf("failed to create user: %w", err)
	}

	token, err := utils.GenerateToken(user.ID, user.Username, s.jwtSecret, 24*time.Hour)
	if err != nil {
		return nil, fmt.Errorf("failed to generate token: %w", err)
	}

	return &AuthResponse{
		Token: token,
		User: UserResponse{
			ID:       user.ID,
			Username: user.Username,
		},
	}, nil
}

func (s *AuthService) SignIn(ctx context.Context, req SignInRequest) (*AuthResponse, error) {
	user, err := s.userRepo.FindByUsername(ctx, req.Username)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrInvalidCredentials
		}
		return nil, fmt.Errorf("failed to query database: %w", err)
	}

	if !utils.CheckPasswordHash(req.Password, user.Password) {
		return nil, utils.ErrInvalidCredentials
	}

	token, err := utils.GenerateToken(user.ID, user.Username, s.jwtSecret, 24*time.Hour)
	if err != nil {
		return nil, fmt.Errorf("failed to generate token: %w", err)
	}

	return &AuthResponse{
		Token: token,
		User: UserResponse{
			ID:       user.ID,
			Username: user.Username,
		},
	}, nil
}
