package handler

import (
	"api/cmd/server/services"
	"api/cmd/server/utils"
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
)

type AuthHandler struct {
	authService *services.AuthService
}

func NewAuthHandler(authService *services.AuthService) *AuthHandler {
	return &AuthHandler{
		authService: authService,
	}
}

func (h *AuthHandler) SignUp(c *gin.Context) {
	var req services.SignUpRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.authService.SignUp(c.Request.Context(), req)
	if err != nil {
		if errors.Is(err, utils.ErrUsernameAlreadyExists) {
			utils.Conflict(c, err.Error())
			return
		}
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "User registered successfully", res)
}

func (h *AuthHandler) SignIn(c *gin.Context) {
	var req services.SignInRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.authService.SignIn(c.Request.Context(), req)
	if err != nil {
		if errors.Is(err, utils.ErrInvalidCredentials) {
			utils.Unauthorized(c, err.Error())
			return
		}
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, "Signed in successfully", res)
}

