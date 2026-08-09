package utils

import "errors"

var (
	ErrUsernameAlreadyExists = errors.New("username is already taken")
	ErrInvalidCredentials    = errors.New("invalid username or password")
	ErrUserNotFound          = errors.New("user not found")
)
