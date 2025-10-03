# BidHub API Reference for Next.js

Quick reference for integrating the Next.js frontend with the Java backend.

## Base URLs

- **API**: `http://localhost:8080/api`
- **WebSocket**: `http://localhost:8080/ws`
- **Swagger**: `http://localhost:8080/swagger-ui.html`

## Authentication Flow

### 1. Register
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "role": "BUYER"  // or "SELLER"
}
```

### 2. Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "BUYER"
  }
}
```

### 3. Get Current User
```bash
GET /api/auth/me
Authorization: Bearer <token>
```

## Auction Items

### Get All Items
```bash
GET /api/items
# No authentication required
```

### Get Active Items Only
```bash
GET /api/items/active
# Returns only items with status ACTIVE
```

### Get Item by ID
```bash
GET /api/items/{id}
```

### Create Item (Seller/Admin)
```bash
POST /api/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Vintage Watch",
  "description": "Beautiful vintage watch from 1950s",
  "startingPrice": 100.00,
  "endTime": "2024-12-31T23:59:59"
}
```

## Bids

### Get All Bids
```bash
GET /api/bids
# No authentication required
```

### Get Bids for Specific Item
```bash
GET /api/bids/item/{itemId}
```

### Get Recent Bids
```bash
GET /api/bids/recent?limit=10
```

### Place Bid (Authenticated)
```bash
POST /api/bids
Authorization: Bearer <token>
Content-Type: application/json

{
  "itemId": 1,
  "amount": 150.00
}

Response:
{
  "id": 1,
  "itemId": 1,
  "userId": 1,
  "username": "johndoe",
  "amount": 150.00,
  "timestamp": "2024-01-15T10:30:00"
}
```

## Admin Endpoints (Admin Role Required)

### Get All Users
```bash
GET /api/admin/users
Authorization: Bearer <admin-token>
```

### Get All Bids (Admin View)
```bash
GET /api/admin/bids
Authorization: Bearer <admin-token>
```

### Get All Items (Admin View)
```bash
GET /api/admin/items
Authorization: Bearer <admin-token>
```

## WebSocket Integration

### Connect to WebSocket
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws');
const client = new Client({
  webSocketFactory: () => socket,
  onConnect: () => console.log('Connected!'),
});

client.activate();
```

### Subscribe to Topics

#### General Auction Updates
```javascript
client.subscribe('/topic/auctions', (message) => {
  const payload = JSON.parse(message.body);
  console.log('Auction update:', payload);
});
```

#### Specific Item Updates
```javascript
client.subscribe('/topic/auctions/{itemId}', (message) => {
  const payload = JSON.parse(message.body);
  console.log('Item update:', payload);
});
```

#### Bid Updates for Item
```javascript
client.subscribe('/topic/bids/{itemId}', (message) => {
  const payload = JSON.parse(message.body);
  console.log('New bid:', payload);
});
```

### WebSocket Message Format
```javascript
{
  "type": "BID_UPDATE" | "AUCTION_UPDATE" | "NEW_BID" | "JOINED_AUCTION",
  "data": { /* item or bid data */ },
  "itemId": 1  // optional
}
```

## Data Models

### Item Response
```typescript
interface ItemResponse {
  id: number;
  name: string;
  description: string;
  startingPrice: number;
  currentPrice: number;
  status: 'ACTIVE' | 'CLOSED' | 'PENDING';
  endTime: string;
  sellerId: number;
  sellerName: string;
  bidCount: number;
  createdAt: string;
}
```

### Bid Response
```typescript
interface BidResponse {
  id: number;
  itemId: number;
  itemName: string;
  userId: number;
  username: string;
  amount: number;
  timestamp: string;
}
```

### User Response
```typescript
interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: 'BUYER' | 'SELLER' | 'ADMIN';
  createdAt: string;
}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "details": ["Field 'amount' must be greater than current price"]
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Item not found with id: 123"
}
```

## Testing with cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer1","password":"password"}'
```

### Get Items
```bash
curl http://localhost:8080/api/items
```

### Place Bid
```bash
curl -X POST http://localhost:8080/api/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"itemId":1,"amount":150.00}'
```

## CORS Configuration

Backend allows requests from:
- `http://localhost:3000` (Next.js dev)
- `http://localhost:3001` (Alternative port)
- `https://yourdomain.com` (Production - update in SecurityConfig.java)

All requests support credentials (cookies/auth headers).

## Rate Limiting

Currently, there is no rate limiting configured. Consider adding rate limiting for production:
- Login attempts: 5 per minute
- Bid submissions: 10 per minute
- API calls: 100 per minute

## Security Notes

1. **JWT Token**: 
   - Expires in 24 hours
   - Store in localStorage or httpOnly cookies
   - Include in Authorization header: `Bearer <token>`

2. **Password Requirements**:
   - Minimum 6 characters (consider increasing for production)
   - BCrypt hashed with strength 10

3. **HTTPS**:
   - Use HTTPS in production
   - WebSocket will use WSS (secure WebSocket)

## Support

For issues or questions:
- Check Swagger UI: `http://localhost:8080/swagger-ui.html`
- View logs: Check console output or `app.log`
- Database: MySQL on port 3306

