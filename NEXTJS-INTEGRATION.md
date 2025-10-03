# Next.js Frontend Integration Guide

## Overview

This backend is now configured to work with a separate Next.js frontend. The Java/Spring Boot backend serves as a **REST API + WebSocket server** only.

## Backend Configuration Changes

### ✅ Completed Changes

1. **Removed WebMvcConfig** - No longer redirects to static index.html
2. **Updated SecurityConfig** - CORS configured for Next.js origins
3. **Cleaned up Security Rules** - Only API endpoints and WebSocket allowed

### 🔧 Backend URLs

- **Backend API**: `http://localhost:8080`
- **REST Endpoints**: `http://localhost:8080/api/*`
- **WebSocket**: `http://localhost:8080/ws`
- **Swagger Docs**: `http://localhost:8080/swagger-ui.html`

---

## Next.js Frontend Setup

### 1. Create Next.js Project

```bash
npx create-next-app@latest bidhub-frontend --typescript --tailwind --app
cd bidhub-frontend
```

### 2. Install Required Dependencies

```bash
npm install axios
npm install sockjs-client @stomp/stompjs
npm install js-cookie  # For JWT token management
```

### 3. Environment Variables

Create `.env.local` in your Next.js project:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```

### 4. API Client Setup

Create `lib/api.ts`:

```typescript
import axios from 'axios';
import Cookies from 'js-cookie';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for CORS with credentials
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken') || Cookies.get('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      Cookies.remove('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 5. WebSocket Client Setup

Create `lib/websocket.ts`:

```typescript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export class WebSocketClient {
  private client: Client | null = null;
  private wsUrl: string;

  constructor() {
    this.wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';
  }

  connect(onConnect: () => void, onError: (error: any) => void) {
    const socket = new SockJS(this.wsUrl);
    
    this.client = new Client({
      webSocketFactory: () => socket as any,
      onConnect: () => {
        console.log('WebSocket connected');
        onConnect();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        onError(frame);
      },
    });

    this.client.activate();
  }

  subscribe(destination: string, callback: (message: any) => void) {
    if (this.client?.connected) {
      return this.client.subscribe(destination, (message) => {
        const payload = JSON.parse(message.body);
        callback(payload);
      });
    }
    return null;
  }

  send(destination: string, body: any) {
    if (this.client?.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }
}

export const wsClient = new WebSocketClient();
```

---

## API Endpoints Reference

### Authentication

```typescript
// Register
POST /api/auth/register
Body: { username: string, email: string, password: string, role: "BUYER" | "SELLER" }

// Login
POST /api/auth/login
Body: { username: string, password: string }
Response: { token: string, user: {...} }

// Get Current User
GET /api/auth/me
Headers: { Authorization: "Bearer <token>" }
```

### Items (Auctions)

```typescript
// Get all items
GET /api/items

// Get active items
GET /api/items/active

// Get item by ID
GET /api/items/{id}

// Create item (SELLER/ADMIN only)
POST /api/items
Body: { name: string, description: string, startingPrice: number, endTime: string }

// Update item (SELLER/ADMIN only)
PUT /api/items/{id}

// Delete item (SELLER/ADMIN only)
DELETE /api/items/{id}
```

### Bids

```typescript
// Get all bids
GET /api/bids

// Get bids for specific item
GET /api/bids/item/{itemId}

// Place a bid (Authenticated users only)
POST /api/bids
Body: { itemId: number, amount: number }
```

### Admin

```typescript
// Get all users (ADMIN only)
GET /api/admin/users

// Other admin endpoints...
GET /api/admin/**
```

---

## WebSocket Topics

### Subscribe to Topics

```typescript
// General auction updates
wsClient.subscribe('/topic/auctions', (message) => {
  console.log('Auction update:', message);
});

// Specific item updates
wsClient.subscribe('/topic/auctions/{itemId}', (message) => {
  console.log('Item update:', message);
});

// Bid updates
wsClient.subscribe('/topic/bids/{itemId}', (message) => {
  console.log('Bid update:', message);
});
```

### WebSocket Message Types

```typescript
interface WebSocketMessage {
  type: 'AUCTION_UPDATE' | 'BID_UPDATE' | 'NEW_BID' | 'JOINED_AUCTION';
  data: any;
  itemId?: number;
}
```

---

## Example Next.js Pages

### Login Page (`app/login/page.tsx`)

```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const { data } = await api.post('/auth/login', { username, password });
      localStorage.setItem('authToken', data.token);
      router.push('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input
        type="text"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Username"
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
      />
      <button type="submit">Login</button>
    </form>
  );
}
```

### Auctions Page (`app/auctions/page.tsx`)

```typescript
'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
import { wsClient } from '@/lib/websocket';

export default function AuctionsPage() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    // Load initial data
    const loadItems = async () => {
      const { data } = await api.get('/items/active');
      setItems(data);
    };
    loadItems();

    // Connect to WebSocket
    wsClient.connect(
      () => {
        wsClient.subscribe('/topic/auctions', (message) => {
          if (message.type === 'AUCTION_UPDATE') {
            // Update items in state
            setItems((prev) =>
              prev.map((item) =>
                item.id === message.data.id ? message.data : item
              )
            );
          }
        });
      },
      (error) => console.error('WebSocket error:', error)
    );

    return () => wsClient.disconnect();
  }, []);

  return (
    <div>
      <h1>Active Auctions</h1>
      {items.map((item: any) => (
        <div key={item.id}>
          <h2>{item.name}</h2>
          <p>Current Price: ${item.currentPrice}</p>
        </div>
      ))}
    </div>
  );
}
```

---

## Running Both Servers

### Terminal 1: Java Backend

```bash
cd /Users/kemaniyoung/Java
./mvnw spring-boot:run
# or
./start-application.sh
```

Backend will run on `http://localhost:8080`

### Terminal 2: Next.js Frontend

```bash
cd bidhub-frontend
npm run dev
```

Frontend will run on `http://localhost:3000`

---

## Production Deployment

### Backend
- Deploy to Heroku, AWS, or any Java hosting
- Update CORS in `SecurityConfig.java` with production URL
- Use environment variables for sensitive data

### Frontend
- Deploy to Vercel (recommended for Next.js)
- Update environment variables with production API URL
- Enable HTTPS for secure WebSocket connections (wss://)

### CORS Update for Production

In `SecurityConfig.java`, update line 94-98:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "https://your-nextjs-app.vercel.app",  // Your production domain
    "https://yourdomain.com"
));
```

---

## Testing the Integration

1. **Start Backend**: `./start-application.sh`
2. **Start Frontend**: `npm run dev`
3. **Test Authentication**: Try login/register
4. **Test WebSocket**: Check browser console for WebSocket connection
5. **Test CORS**: Make API calls from Next.js

---

## Troubleshooting

### CORS Issues
- Check browser console for CORS errors
- Verify frontend URL is in `SecurityConfig.java` allowed origins
- Ensure `withCredentials: true` in axios config

### WebSocket Issues
- Check if backend is running on port 8080
- Verify WebSocket endpoint: `http://localhost:8080/ws`
- Check browser console for connection errors

### Authentication Issues
- Ensure JWT token is stored in localStorage
- Check if token is included in request headers
- Verify token hasn't expired (24 hours default)

---

## Next Steps

1. Create your Next.js project structure
2. Set up authentication flow
3. Implement auction listing pages
4. Add WebSocket real-time updates
5. Style with Tailwind CSS
6. Deploy both services

Happy coding! 🚀

