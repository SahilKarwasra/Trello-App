package websockets

import (
	"time"

	"github.com/google/uuid"
)

type EventType string

const (
	EventSectionCreated EventType = "SECTION_CREATED"
	EventSectionUpdated EventType = "SECTION_UPDATED"
	EventSectionMoved   EventType = "SECTION_MOVED"
	EventSectionDeleted EventType = "SECTION_DELETED"

	EventIssueCreated EventType = "ISSUE_CREATED"
	EventIssueUpdated EventType = "ISSUE_UPDATED"
	EventIssueMoved   EventType = "ISSUE_MOVED"
	EventIssueDeleted EventType = "ISSUE_DELETED"

	EventUserJoined EventType = "USER_JOINED"
	EventUserLeft   EventType = "USER_LEFT"
	EventRoomState  EventType = "ROOM_STATE"
)

type UserPresence struct {
	UserID   uuid.UUID `json:"user_id"`
	Username string    `json:"username"`
}

type RoomStatePayload struct {
	OnlineCount int            `json:"online_count"`
	ActiveUsers []UserPresence `json:"active_users"`
}

type UserPresencePayload struct {
	UserID      uuid.UUID `json:"user_id"`
	Username    string    `json:"username"`
	OnlineCount int       `json:"online_count"`
}

type BoardEvent struct {
	Type      EventType   `json:"type"`
	BoardID   string      `json:"board_id"`
	ActorID   string      `json:"actor_id"`
	Timestamp time.Time   `json:"timestamp"`
	Payload   interface{} `json:"payload"`
}

type Broadcaster interface {
	Broadcast(boardID string, actorID uuid.UUID, eventType EventType, payload interface{})
}
