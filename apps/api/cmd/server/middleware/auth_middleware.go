package middleware

import (
	"api/cmd/server/utils"
	"strings"

	"github.com/gin-gonic/gin"
)

// AuthMiddleware validates JWT Bearer tokens in the Authorization header.
func AuthMiddleware(jwtSecret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.Unauthorized(c, utils.ErrMissingToken.Error())
			c.Abort()
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			utils.Unauthorized(c, utils.ErrInvalidToken.Error())
			c.Abort()
			return
		}

		tokenString := strings.TrimSpace(parts[1])
		claims, err := utils.ValidateToken(tokenString, jwtSecret)
		if err != nil {
			utils.Unauthorized(c, utils.ErrInvalidToken.Error())
			c.Abort()
			return
		}

		c.Set("userID", claims.UserID)
		c.Set("username", claims.Username)
		c.Set("claims", claims)

		c.Next()
	}
}
