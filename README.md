# Bidding WebSocket API

A Spring Boot application that provides a REST API for bidding with real-time WebSocket updates.

## Features

- **REST API** for bidding operations
- **WebSocket** for live updates
- **Real-time bidding** with instant notifications
- **Auction management** with automatic expiration
- **H2 Database** for easy development and testing
- **Swagger/OpenAPI** documentation
- **Sample data** initialization

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring WebSocket
- Spring Data JPA
- H2 Database
- Swagger/OpenAPI 3
- Maven

## Getting Started

### Option 1: Docker (Recommended)

1. Clone or download the project
2. Navigate to the project directory
3. Build and run with Docker Compose:
   ```bash
   docker-compose up --build
   ```

4. The application will start on `http://localhost:8080`

### Option 2: Local Development

#### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

#### Running the Application
1. Clone or download the project
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. The application will start on `http://localhost:8080`

### Alternative: Build and Run JAR

```bash
# Build the application
mvn -DskipTests package

# Run the JAR file
java -jar target/bidding-websocket-api-1.0.0.jar
```

### Docker Commands

```bash
# Build and start the container
docker-compose up --build

# Run in background
docker-compose up -d --build

# Stop the container
docker-compose down

# View logs
docker-compose logs -f

# Rebuild without cache
docker-compose build --no-cache
```

### Service Management

#### Kill and Restart Java Service

**Kill the current Java process:**
```bash
# Option A: Kill by process name
pkill -f "BiddingWebSocketApplication"

# Option B: Kill by port (if using port 8080)
lsof -ti:8080 | xargs kill -9

# Option C: Find and kill manually
ps aux | grep java | grep BiddingWebSocketApplication
kill -9 <PID>
```

**Restart the service:**
```bash
# Using Maven (recommended)
cd /Users/kemaniyoung/Java
export PATH="/tmp/apache-maven-3.9.11/bin:$PATH"
mvn spring-boot:run

# Using JAR file
mvn clean package -DskipTests
java -jar target/bidding-websocket-api-1.0.0.jar
```

**One-liner to kill and restart:**
```bash
pkill -f "BiddingWebSocketApplication" && sleep 2 && cd /Users/kemaniyoung/Java && export PATH="/tmp/apache-maven-3.9.11/bin:$PATH" && mvn spring-boot:run
```

**Check if service is running:**
```bash
# Check if port 8080 is in use
lsof -i :8080

# Check Java processes
ps aux | grep java | grep -v grep

# Test the API
curl http://localhost:8080/api/items
```

### Change Port (if needed)

If port 8080 is already in use:

```bash
# Using mvn
SERVER_PORT=8081 mvn spring-boot:run

# Or with the jar
java -jar target/bidding-websocket-api-1.0.0.jar --server.port=8081

# Or with Docker (modify docker-compose.yml ports section)
# Change "8080:8080" to "8081:8080"
```

### Access Points

- **REST API**: `http://localhost:8080/api/`
- **Web Interface**: `http://localhost:8080/`
- **API Documentation**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`
- **Database**: MySQL on `localhost:3306`
  - Database Name: `bidding_db`
  - Username: `root`
  - Password: `password`

## API Endpoints

### Items
- `GET /api/items` - Get all items
- `GET /api/items/active` - Get active auction items
- `GET /api/items/{id}` - Get item by ID
- `POST /api/items` - Create new item
- `PUT /api/items/{id}` - Update item
- `DELETE /api/items/{id}` - Delete item

### Bids
- `POST /api/bids` - Place a new bid
- `GET /api/bids/item/{itemId}` - Get all bids for an item
- `GET /api/bids/item/{itemId}/winning` - Get winning bid for an item
- `GET /api/bids/item/{itemId}/count` - Get bid count for an item
- `GET /api/bids/item/{itemId}/top?limit=10` - Get top bids for an item

## WebSocket Endpoints

- **Connection**: `/ws`
- **Topics**:
  - `/topic/auctions` - General auction updates
  - `/topic/bids/{itemId}` - Specific item bid updates
  - `/topic/auctions/status` - Auction status updates

## Sample Data

The application automatically creates sample auction items on startup:
- Vintage Guitar (24 hours)
- Antique Watch (48 hours)
- Art Painting (12 hours)

## Testing the Application

1. Open `http://localhost:8080/` in your browser
2. You'll see the live bidding interface
3. Select an item and place bids
4. Open multiple browser tabs to see real-time updates

## WebSocket Message Format

```json
{
  "type": "NEW_BID",
  "data": {
    "id": 1,
    "bidderName": "John Doe",
    "amount": 150.00,
    "itemId": 1,
    "timestamp": "2023-12-01T10:30:00",
    "isWinning": true
  },
  "itemId": 1,
  "status": "ACTIVE"
}
```

## Configuration

The application is configured to use MySQL database with JDBC connectivity. To use a different database, update `application.properties`:

```properties
# Example for PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/bidding_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Database Setup

Before running the application, ensure MySQL is running and create the database:

```sql
CREATE DATABASE bidding_db;
```

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/example/bidding/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST and WebSocket controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   └── BiddingWebSocketApplication.java
│   └── resources/
│       ├── static/         # Static web content
│       └── application.properties
```

## License

This project is for educational purposes.
