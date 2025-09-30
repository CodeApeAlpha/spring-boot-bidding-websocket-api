# WebSocket and Application Flow

This document explains how data flows through the system for both WebSocket (real-time) and REST paths, mapped to files in the project structure.

## Overview

- WebSocket endpoint: `/ws` (SockJS/STOMP)
- App destination prefix: `/app/**`
- Broker destination prefix: `/topic/**`
- REST API base paths: `/api/items`, `/api/bids`
- Database: MySQL via Spring Data JPA

Key files:
- `config/WebSocketConfig.java`: Defines `/ws`, sets `/app` and `/topic` prefixes
- `controller/WebSocketController.java`: STOMP handlers and broadcast helpers
- `controller/BidController.java`, `controller/ItemController.java`: REST controllers
- `service/BidService.java`, `service/ItemService.java`: Business logic and broadcasts
- `model/*`: JPA entities (`Item`, `Bid`, `AuctionStatus`)
- `dto/*`: REST and WebSocket payloads (`BidRequest`, `BidResponse`, `WebSocketMessage`)

## Client → WebSocket Connect

1) Client creates a SockJS/STOMP connection to `/ws`.
2) Client subscribes to destinations:
   - `/topic/auctions` (global auction feed)
   - `/topic/bids/{itemId}` (item-specific bid updates)
   - `/topic/auctions/status` (current auction status snapshot)
3) Client can send messages to app endpoints (handled by server):
   - `/app/auctions/status` → returns a status snapshot
   - `/app/auctions/join` → acknowledges joining a specific auction room

Relevant code:
- `config/WebSocketConfig.java`
- `controller/WebSocketController.java`

## Message Routing (WebSocket)

- `@MessageMapping("/auctions/status")` → `getAuctionStatus()`
  - Fetches active items via `ItemService`
  - Returns `WebSocketMessage("AUCTION_STATUS", activeItems)`
  - Broker sends to `/topic/auctions/status`

- `@MessageMapping("/auctions/join")` → `joinAuction(itemId)`
  - Acknowledges join with `WebSocketMessage("JOINED_AUCTION", ..., itemId)`
  - Sends to `/topic/auctions/{itemId}` using `SimpMessagingTemplate`

## REST → Service → DB → WebSocket Broadcast

Typical write flow: placing a bid
1) Client sends HTTP POST to `/api/bids` with `BidRequest`
2) `BidController.placeBid` delegates to `BidService.placeBid`
3) `BidService` validates (
   - Item exists and is `ACTIVE`
   - Auction not ended
   - Amount >= (currentHighestBid + 0.01) or startingPrice
  )
4) Service persists the `Bid`, updates the `Item.currentHighestBid`, updates winning state
5) Service broadcasts WebSocket updates using `SimpMessagingTemplate`:
   - `/topic/bids/{itemId}`: `WebSocketMessage("NEW_BID", BidResponse, itemId)`
   - `/topic/auctions`: `WebSocketMessage("BID_UPDATE", BidResponse)`

Relevant code:
- `controller/BidController.java`
- `service/BidService.java`
- `repository/BidRepository`, `repository/ItemRepository`

## Data Shapes (DTOs)

- `BidRequest` (REST in): bidderName, amount, itemId
- `BidResponse` (REST/WebSocket out): id, bidderName, amount, itemId, timestamp, isWinning
- `WebSocketMessage` (WebSocket out): type, data, itemId?, status?

## Domain and Persistence

- `Item`: name, description, startingPrice, currentHighestBid, startTime, endTime, status
- `Bid`: bidderName, amount, itemId, timestamp, isWinning
- `AuctionStatus`: `ACTIVE | ENDED | CANCELLED`

Persistence layer:
- Spring Data JPA repositories (`BidRepository`, `ItemRepository`) handle CRUD/queries
- Hibernate maps entities to MySQL (see `application.properties`)

## Configuration

- `config/WebSocketConfig.java`:
  - STOMP endpoint: `/ws`
  - App prefix: `/app`
  - Broker: `/topic/**`

- `src/main/resources/application.properties`:
  - Server port, JDBC URL/credentials
  - JPA/Hibernate options
  - Optional WebSocket and logging levels

## Real-Time Scenarios

Placing a bid (end-to-end):
- HTTP POST `/api/bids` → Controller → Service → DB → broadcast to `/topic/bids/{itemId}` and `/topic/auctions`

Getting auction status snapshot:
- STOMP SEND `/app/auctions/status` → `getAuctionStatus()` → broker to `/topic/auctions/status`

Joining an auction room:
- STOMP SEND `/app/auctions/join` with `itemId` → ack to `/topic/auctions/{itemId}`

## Observability and Logs

To watch WebSocket events (in container):
```bash
docker logs -f <container-name> | grep -E "(WebSocket|STOMP|CONNECT|SUBSCRIBE|MESSAGE)"
```

Enable more verbose logging by adding to `application.properties`:
```properties
logging.level.org.springframework.web.socket=DEBUG
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.messaging.simp=DEBUG
logging.level.org.springframework.messaging.simp.broker=DEBUG
logging.level.org.springframework.messaging.simp.stomp=DEBUG
```

## Quick Map from Flow to Files

- WebSocket entry: `config/WebSocketConfig.java`, `controller/WebSocketController.java`
- REST entry: `controller/BidController.java`, `controller/ItemController.java`
- Business rules + broadcasts: `service/BidService.java`, `service/ItemService.java`
- Data shapes: `dto/*`
- Entities: `model/*`
- Persistence: `repository/*`
- Configuration: `application.properties`


