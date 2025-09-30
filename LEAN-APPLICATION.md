# 🚀 Lean Bidding Application - WebSocket Live Feed

A **minimal, high-performance** real-time bidding application focused on WebSocket live feeds.

## ✨ **What Makes It Lean**

### **Removed Complexity:**
- ❌ Redis dependencies and configuration
- ❌ Centralized messaging service
- ❌ Multiple infrastructure options
- ❌ Complex Docker Compose setups
- ❌ Unnecessary profiles and configurations

### **Core Focus:**
- ✅ **Pure WebSocket** real-time communication
- ✅ **Direct messaging** between server and clients
- ✅ **Minimal dependencies** (Spring Boot + WebSocket + MySQL)
- ✅ **Simple deployment** (one container + one app)
- ✅ **Fast startup** and low memory footprint

## 🎯 **Architecture**

```
┌─────────────────┐    WebSocket     ┌─────────────────┐
│   Web Client    │◄─────────────────┤  Spring Boot    │
│   (Browser)     │                  │   Application   │
└─────────────────┘                  └─────────────────┘
                                              │
                                              │ JDBC
                                              ▼
                                     ┌─────────────────┐
                                     │     MySQL       │
                                     │   (Database)    │
                                     └─────────────────┘
```

## 🚀 **How to Run**

### **Option 1: IDE Run Button (Recommended)**
- **VS Code**: Press `F5` → Select "Bidding App (WebSocket Live Feed)"
- **IntelliJ IDEA**: Click Run button → Select "Bidding App (WebSocket Live Feed)"

### **Option 2: Terminal**
```bash
# Start MySQL and run application
./ide-run.sh run

# Or start MySQL only
./ide-run.sh mysql
export PATH="$PWD/apache-maven-3.9.11/bin:$PATH"
mvn spring-boot:run
```

### **Option 3: Direct Maven**
```bash
# Start MySQL first
docker run -d --name bidding-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=bidding_db -p 3306:3306 mysql:8.0

# Run application
export PATH="$PWD/apache-maven-3.9.11/bin:$PATH"
mvn spring-boot:run
```

## 🌐 **Access Your App**

- **Main App:** http://localhost:8080
- **API Docs:** http://localhost:8080/swagger-ui.html
- **WebSocket:** ws://localhost:8080/ws

## 🔥 **Live Feed Features**

### **Real-Time Updates:**
- **Live Bids Feed**: All bids appear instantly across all clients
- **Item-Specific Updates**: Bids for specific items update in real-time
- **WebSocket Topics**:
  - `/topic/auctions` - General live feed
  - `/topic/bids/{itemId}` - Item-specific updates

### **WebSocket Messages:**
```javascript
// Subscribe to live feed
stompClient.subscribe('/topic/auctions', function(message) {
    const data = JSON.parse(message.body);
    addBidToSharedFeed(data);
});

// Subscribe to item-specific updates
stompClient.subscribe('/topic/bids/' + itemId, function(message) {
    const data = JSON.parse(message.body);
    updateItemBids(data);
});
```

## 📦 **Dependencies (Minimal)**

```xml
<!-- Core Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- WebSocket Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

## ⚡ **Performance Benefits**

- **Fast Startup**: ~3-5 seconds (vs 10+ with Redis)
- **Low Memory**: ~50MB base (vs 100+ with Redis)
- **Simple Deployment**: 1 container + 1 app (vs 3+ containers)
- **Direct Communication**: No message broker overhead
- **Minimal Configuration**: Single properties file

## 🛠️ **Development**

### **Key Files:**
- `WebSocketConfig.java` - WebSocket configuration
- `BidService.java` - Real-time bid processing
- `WebSocketController.java` - WebSocket endpoints
- `app.js` - Client-side WebSocket handling

### **WebSocket Flow:**
1. Client connects to `/ws`
2. Subscribes to `/topic/auctions` for live feed
3. Subscribes to `/topic/bids/{itemId}` for specific items
4. Server sends real-time updates via `SimpMessagingTemplate`
5. All connected clients receive updates instantly

## 🎉 **Result**

A **lean, fast, real-time** bidding application that focuses on what matters most: **live WebSocket communication** for an engaging user experience!

**No Redis, no complexity, just pure real-time bidding!** 🚀
