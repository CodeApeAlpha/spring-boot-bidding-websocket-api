# IDE Setup Guide

## 🚀 Running from IDE (IntelliJ IDEA, Eclipse, VS Code)

### Option 1: With Full Infrastructure (Recommended)
1. **Start Infrastructure First:**
   ```bash
   ./start-application.sh
   ```
   This starts Redis and MySQL containers.

2. **Run from IDE:**
   - Use the **Run** button in your IDE
   - Or run `BiddingWebSocketApplication.main()` method
   - The app will use centralized messaging with Redis

### Option 2: IDE-Only Mode (Simpler)
1. **Start Only MySQL:**
   ```bash
   docker run -d --name bidding-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=bidding_db -p 3306:3306 mysql:8.0
   ```

2. **Run from IDE:**
   - Use the **Run** button in your IDE
   - The app will work with local messaging only (no Redis)

## 🔧 IDE Configuration

### IntelliJ IDEA
1. **Run Configuration:**
   - Main Class: `com.example.bidding.BiddingWebSocketApplication`
   - VM Options: `-Dspring.profiles.active=ide` (optional)
   - Program Arguments: (leave empty)

2. **Maven Integration:**
   - Right-click `pom.xml` → "Add as Maven Project"
   - Use Maven run configuration: `spring-boot:run`

### Eclipse
1. **Import Project:**
   - File → Import → Existing Maven Projects
   - Select the project folder

2. **Run Configuration:**
   - Right-click project → Run As → Java Application
   - Select `BiddingWebSocketApplication`

### VS Code
1. **Install Extensions:**
   - Extension Pack for Java
   - Spring Boot Extension Pack

2. **Run Configuration:**
   - Open `BiddingWebSocketApplication.java`
   - Click "Run" above the main method

## 🐛 Troubleshooting

### Common Issues:

1. **"Connection refused" errors:**
   - Make sure MySQL is running: `docker ps | grep mysql`
   - Check if port 3306 is available

2. **"Redis connection failed":**
   - This is normal if Redis isn't running
   - App will fallback to local messaging

3. **"Port 8080 already in use":**
   - Stop the terminal version: `pkill -f BiddingWebSocketApplication`
   - Or change port in `application.properties`

4. **IDE can't find main class:**
   - Clean and rebuild project
   - Refresh Maven dependencies

## 📊 What You'll See:

### With Full Infrastructure:
- ✅ Centralized messaging across multiple instances
- ✅ Redis for cross-instance communication
- ✅ Full real-time capabilities

### IDE-Only Mode:
- ✅ Local WebSocket messaging
- ✅ Single instance operation
- ✅ All core features work

## 🎯 Testing:

1. **Open browser:** `http://localhost:8080`
2. **Place bids** and watch real-time updates
3. **Check API docs:** `http://localhost:8080/swagger-ui.html`

The application will work in both modes - choose based on your needs!
