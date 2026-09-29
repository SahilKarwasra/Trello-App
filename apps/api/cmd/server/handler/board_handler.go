package handler

import (
	"api/cmd/server/services"
	"api/cmd/server/utils"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type BoardHandler struct {
	boardService *services.BoardService
}

func NewBoardHandler(boardService *services.BoardService) *BoardHandler {
	return &BoardHandler{
		boardService: boardService,
	}
}

func (h *BoardHandler) CreateBoard(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	userID, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.BoardRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.boardService.CreateBoard(c.Request.Context(), userID, req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, "Board created successfully", res)
}

func (h *BoardHandler) GetBoards(c *gin.Context) {
	val, exists := c.Get("userID")
	if !exists {
		utils.Unauthorized(c, "unauthorized")
		return
	}

	_, ok := val.(uuid.UUID)
	if !ok {
		utils.Unauthorized(c, "invalid user id in context")
		return
	}

	var req services.GetBoardsRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.BadRequest(c, utils.FormatValidationError(err))
		return
	}

	res, err := h.boardService.GetBoards(c.Request.Context(), req)
	if err != nil {
		utils.InternalServerError(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, "Boards retrieved successfully", res)
}
