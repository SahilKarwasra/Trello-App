package handler

import (
	"api/cmd/server/utils"
	"api/cmd/server/websockets"
	"log"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow connections from frontend clients (KMP desktop/mobile/web)
	},
}

type WSHandler struct {
	hub       *websockets.Hub
	jwtSecret string
}

func NewWSHandler(hub *websockets.Hub, jwtSecret string) *WSHandler {
	return &WSHandler{
		hub:       hub,
		jwtSecret: jwtSecret,
	}
}

func (h *WSHandler) ServeWS(c *gin.Context) {
	boardID := strings.TrimSpace(c.Query("board_id"))
	if boardID == "" {
		utils.BadRequest(c, "board_id query parameter is required")
		return
	}

	token := strings.TrimSpace(c.Query("token"))
	if token == "" {
		authHeader := c.GetHeader("Authorization")
		if authHeader != "" {
			parts := strings.SplitN(authHeader, " ", 2)
			if len(parts) == 2 && strings.EqualFold(parts[0], "Bearer") {
				token = strings.TrimSpace(parts[1])
			}
		}
	}

	if token == "" {
		utils.Unauthorized(c, "authentication token is required (via token query param or Bearer header)")
		return
	}

	claims, err := utils.ValidateToken(token, h.jwtSecret)
	if err != nil {
		utils.Unauthorized(c, "invalid or expired token")
		return
	}

	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		log.Printf("[WS] Failed to upgrade websocket: %v", err)
		return
	}

	client := websockets.NewClient(h.hub, conn, claims.UserID, claims.Username, boardID)
	h.hub.RegisterClient(client)

	go client.WritePump()
	go client.ReadPump()
}
