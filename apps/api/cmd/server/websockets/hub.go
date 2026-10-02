package websockets

import (
	"encoding/json"
	"log"
	"sync"
	"time"

	"github.com/google/uuid"
)

type BroadcastMessage struct {
	BoardID string
	ActorID uuid.UUID
	Event   BoardEvent
}

type Hub struct {
	// rooms maps boardID -> set of connected *Client
	rooms      map[string]map[*Client]bool
	register   chan *Client
	unregister chan *Client
	broadcast  chan BroadcastMessage
	mu         sync.RWMutex
}

func NewHub() *Hub {
	return &Hub{
		rooms:      make(map[string]map[*Client]bool),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		broadcast:  make(chan BroadcastMessage, 256),
	}
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.mu.Lock()
			if _, ok := h.rooms[client.BoardID]; !ok {
				h.rooms[client.BoardID] = make(map[*Client]bool)
			}
			h.rooms[client.BoardID][client] = true

			// 1. Calculate unique active users for this board
			activeUsersMap := make(map[uuid.UUID]string)
			for c := range h.rooms[client.BoardID] {
				activeUsersMap[c.UserID] = c.Username
			}

			activeUsersList := make([]UserPresence, 0, len(activeUsersMap))
			for id, username := range activeUsersMap {
				activeUsersList = append(activeUsersList, UserPresence{
					UserID:   id,
					Username: username,
				})
			}
			onlineCount := len(activeUsersList)

			log.Printf("[WS] User %s (%s) joined board %s (online users: %d)", client.UserID, client.Username, client.BoardID, onlineCount)

			// 2. Send current room snapshot to the newly joined client
			roomStateEvent := BoardEvent{
				Type:      EventRoomState,
				BoardID:   client.BoardID,
				ActorID:   client.UserID.String(),
				Timestamp: time.Now().UTC(),
				Payload: RoomStatePayload{
					OnlineCount: onlineCount,
					ActiveUsers: activeUsersList,
				},
			}
			if stateBytes, err := json.Marshal(roomStateEvent); err == nil {
				select {
				case client.Send <- stateBytes:
				default:
				}
			}

			// 3. Broadcast USER_JOINED to all other clients in this room
			userJoinedEvent := BoardEvent{
				Type:      EventUserJoined,
				BoardID:   client.BoardID,
				ActorID:   client.UserID.String(),
				Timestamp: time.Now().UTC(),
				Payload: UserPresencePayload{
					UserID:      client.UserID,
					Username:    client.Username,
					OnlineCount: onlineCount,
				},
			}
			if joinedBytes, err := json.Marshal(userJoinedEvent); err == nil {
				for otherClient := range h.rooms[client.BoardID] {
					if otherClient != client {
						select {
						case otherClient.Send <- joinedBytes:
						default:
							close(otherClient.Send)
							delete(h.rooms[client.BoardID], otherClient)
						}
					}
				}
			}
			h.mu.Unlock()

		case client := <-h.unregister:
			h.mu.Lock()
			if clients, ok := h.rooms[client.BoardID]; ok {
				if _, exists := clients[client]; exists {
					delete(clients, client)
					close(client.Send)
				}

				// Calculate remaining unique users
				activeUsersMap := make(map[uuid.UUID]string)
				for c := range clients {
					activeUsersMap[c.UserID] = c.Username
				}
				onlineCount := len(activeUsersMap)

				if len(clients) == 0 {
					delete(h.rooms, client.BoardID)
					log.Printf("[WS] Board room %s is now empty and closed", client.BoardID)
				} else {
					log.Printf("[WS] User %s (%s) left board %s (remaining users: %d)", client.UserID, client.Username, client.BoardID, onlineCount)

					// Broadcast USER_LEFT to remaining clients in this board room
					userLeftEvent := BoardEvent{
						Type:      EventUserLeft,
						BoardID:   client.BoardID,
						ActorID:   client.UserID.String(),
						Timestamp: time.Now().UTC(),
						Payload: UserPresencePayload{
							UserID:      client.UserID,
							Username:    client.Username,
							OnlineCount: onlineCount,
						},
					}
					if leftBytes, err := json.Marshal(userLeftEvent); err == nil {
						for remainingClient := range clients {
							select {
							case remainingClient.Send <- leftBytes:
							default:
								close(remainingClient.Send)
								delete(clients, remainingClient)
							}
						}
					}
				}
			}
			h.mu.Unlock()

		case message := <-h.broadcast:
			h.mu.RLock()
			clients, ok := h.rooms[message.BoardID]
			if !ok || len(clients) == 0 {
				h.mu.RUnlock()
				continue
			}

			payloadBytes, err := json.Marshal(message.Event)
			if err != nil {
				log.Printf("[WS] Error marshaling event: %v", err)
				h.mu.RUnlock()
				continue
			}

			for client := range clients {
				select {
				case client.Send <- payloadBytes:
				default:
					close(client.Send)
					delete(clients, client)
				}
			}
			h.mu.RUnlock()
		}
	}
}

func (h *Hub) RegisterClient(client *Client) {
	h.register <- client
}

func (h *Hub) UnregisterClient(client *Client) {
	h.unregister <- client
}

// Broadcast dispatches a typed BoardEvent to all clients joined to boardID.
func (h *Hub) Broadcast(boardID string, actorID uuid.UUID, eventType EventType, payload interface{}) {
	h.broadcast <- BroadcastMessage{
		BoardID: boardID,
		ActorID: actorID,
		Event: BoardEvent{
			Type:      eventType,
			BoardID:   boardID,
			ActorID:   actorID.String(),
			Timestamp: time.Now().UTC(),
			Payload:   payload,
		},
	}
}
