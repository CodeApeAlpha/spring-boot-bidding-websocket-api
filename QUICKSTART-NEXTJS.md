# Quick Start: Next.js Frontend for BidHub

## ✅ Backend Status

Your Java backend is ready! Compiled successfully with:
- ✅ API endpoints at `http://localhost:8080/api`
- ✅ WebSocket at `http://localhost:8080/ws`
- ✅ CORS configured for Next.js
- ✅ JWT authentication ready

## 🚀 Create Next.js Frontend (Quick Setup)

### Step 1: Create Next.js App

```bash
# Navigate to parent directory
cd /Users/kemaniyoung

# Create Next.js app (will create 'bidhub-frontend' directory)
npx create-next-app@latest bidhub-frontend
```

When prompted, select:
- ✅ TypeScript: Yes
- ✅ ESLint: Yes
- ✅ Tailwind CSS: Yes
- ✅ `src/` directory: Yes
- ✅ App Router: Yes
- ✅ Import alias: Yes (@/*)

### Step 2: Install Dependencies

```bash
cd bidhub-frontend
npm install axios sockjs-client @stomp/stompjs
npm install -D @types/sockjs-client
```

### Step 3: Create Environment File

Create `.env.local`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```

### Step 4: Create API Client

Create `src/lib/api.ts`:
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Step 5: Create WebSocket Client

Create `src/lib/websocket.ts`:
```typescript
'use client';

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
```

### Step 6: Create Login Page

Create `src/app/login/page.tsx`:
```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const { data } = await api.post('/auth/login', { username, password });
      localStorage.setItem('authToken', data.token);
      router.push('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 to-gray-800">
      <div className="bg-white p-8 rounded-lg shadow-xl w-96">
        <h1 className="text-3xl font-bold text-center mb-6 text-gray-800">
          BidHub Login
        </h1>
        
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-gray-900"
              required
            />
          </div>

          <div className="mb-6">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-gray-900"
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-lg transition duration-200 disabled:opacity-50"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="text-center mt-4 text-gray-600 text-sm">
          Don't have an account?{' '}
          <a href="/register" className="text-blue-500 hover:underline">
            Register
          </a>
        </p>
      </div>
    </div>
  );
}
```

### Step 7: Create Dashboard Page

Create `src/app/dashboard/page.tsx`:
```typescript
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import { WebSocketClient } from '@/lib/websocket';

interface Item {
  id: number;
  name: string;
  description: string;
  currentPrice: number;
  status: string;
  endTime: string;
  bidCount: number;
}

export default function DashboardPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [wsConnected, setWsConnected] = useState(false);
  const router = useRouter();
  const [wsClient] = useState(() => new WebSocketClient());

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      router.push('/login');
      return;
    }

    loadItems();
    connectWebSocket();

    return () => wsClient.disconnect();
  }, []);

  const loadItems = async () => {
    try {
      const { data } = await api.get('/items/active');
      setItems(data);
    } catch (error) {
      console.error('Failed to load items:', error);
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    wsClient.connect(
      () => {
        setWsConnected(true);
        wsClient.subscribe('/topic/auctions', (message) => {
          console.log('WebSocket message:', message);
          if (message.type === 'AUCTION_UPDATE' || message.type === 'BID_UPDATE') {
            loadItems(); // Reload items on updates
          }
        });
      },
      (error) => {
        console.error('WebSocket error:', error);
        setWsConnected(false);
      }
    );
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    router.push('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900">
        <div className="text-white text-xl">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <nav className="bg-gray-800 p-4 flex justify-between items-center">
        <h1 className="text-2xl font-bold">BidHub Dashboard</h1>
        <div className="flex items-center gap-4">
          <div className={`w-3 h-3 rounded-full ${wsConnected ? 'bg-green-500' : 'bg-red-500'}`} />
          <span className="text-sm">{wsConnected ? 'Connected' : 'Disconnected'}</span>
          <button
            onClick={handleLogout}
            className="bg-red-500 hover:bg-red-600 px-4 py-2 rounded"
          >
            Logout
          </button>
        </div>
      </nav>

      <div className="container mx-auto p-8">
        <h2 className="text-3xl font-bold mb-6">Active Auctions</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item) => (
            <div key={item.id} className="bg-gray-800 rounded-lg p-6 hover:bg-gray-700 transition">
              <h3 className="text-xl font-bold mb-2">{item.name}</h3>
              <p className="text-gray-400 mb-4">{item.description}</p>
              <div className="flex justify-between items-center">
                <span className="text-2xl font-bold text-green-400">
                  ${item.currentPrice}
                </span>
                <span className="text-sm text-gray-400">
                  {item.bidCount} bids
                </span>
              </div>
              <button className="mt-4 w-full bg-blue-500 hover:bg-blue-600 py-2 rounded">
                Place Bid
              </button>
            </div>
          ))}
        </div>

        {items.length === 0 && (
          <div className="text-center text-gray-400 mt-12">
            <p className="text-xl">No active auctions at the moment</p>
          </div>
        )}
      </div>
    </div>
  );
}
```

### Step 8: Update Home Page

Replace `src/app/page.tsx`:
```typescript
'use client';

import { useRouter } from 'next/navigation';

export default function Home() {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-white mb-4">
          Welcome to BidHub
        </h1>
        <p className="text-xl text-gray-300 mb-8">
          Real-time auction platform with WebSocket updates
        </p>
        <div className="flex gap-4 justify-center">
          <button
            onClick={() => router.push('/login')}
            className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-8 rounded-lg text-lg transition"
          >
            Login
          </button>
          <button
            onClick={() => router.push('/register')}
            className="bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-8 rounded-lg text-lg transition"
          >
            Register
          </button>
        </div>
      </div>
    </div>
  );
}
```

## 🏃 Run Both Servers

### Terminal 1: Start Backend
```bash
cd /Users/kemaniyoung/Java
./start-application.sh
```
Backend runs on: **http://localhost:8080**

### Terminal 2: Start Frontend
```bash
cd /Users/kemaniyoung/bidhub-frontend
npm run dev
```
Frontend runs on: **http://localhost:3000**

## 🧪 Test the Integration

1. **Open browser**: http://localhost:3000
2. **Login** with test credentials:
   - Username: `buyer1`
   - Password: `password`
3. **View auctions** in dashboard
4. **Check WebSocket**: Green dot = connected
5. **Watch real-time updates** when bids are placed

## 📝 Test Users (From Backend)

- **Buyer**: `buyer1` / `password`
- **Seller**: `seller1` / `password`
- **Admin**: `admin` / `password`

## 🎉 You're Done!

Your full-stack auction app is running with:
- ✅ Next.js frontend with TypeScript & Tailwind
- ✅ Spring Boot backend with JWT auth
- ✅ Real-time WebSocket updates
- ✅ Beautiful, modern UI

## 🔗 Useful Links

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Swagger Docs: http://localhost:8080/swagger-ui.html
- GitHub: https://github.com/CodeApeAlpha/spring-boot-bidding-websocket-api

Need help? Check `NEXTJS-INTEGRATION.md` or `API-REFERENCE.md`!

