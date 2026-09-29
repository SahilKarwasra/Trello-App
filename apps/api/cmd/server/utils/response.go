package utils

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

type APIResponse struct {
	StatusCode int         `json:"statusCode"`
	Data       interface{} `json:"data"`
	IsSuccess  bool        `json:"isSuccess"`
	Message    string      `json:"message"`
}

func Success(c *gin.Context, statusCode int, message string, data interface{}) {
	if data == nil {
		data = gin.H{}
	}
	c.JSON(statusCode, APIResponse{
		StatusCode: statusCode,
		Data:       data,
		IsSuccess:  true,
		Message:    message,
	})
}

func Error(c *gin.Context, statusCode int, message string) {
	c.JSON(statusCode, APIResponse{
		StatusCode: statusCode,
		Data:       gin.H{},
		IsSuccess:  false,
		Message:    message,
	})
}

func BadRequest(c *gin.Context, message string) {
	Error(c, http.StatusBadRequest, message)
}

func Unauthorized(c *gin.Context, message string) {
	Error(c, http.StatusUnauthorized, message)
}

func Forbidden(c *gin.Context, message string) {
	Error(c, http.StatusForbidden, message)
}

func InternalServerError(c *gin.Context, message string) {
	Error(c, http.StatusInternalServerError, message)
}

func NotFound(c *gin.Context, message string) {
	Error(c, http.StatusNotFound, message)
}

func Conflict(c *gin.Context, message string) {
	Error(c, http.StatusConflict, message)
}
