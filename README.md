# Bidding WebSocket API

A Spring Boot application that provides a REST API for bidding with real-time WebSocket updates.

## Features

- **REST API** for bidding operations
- **WebSocket** for live updates
- **Real-time bidding** with instant notifications
- **Auction management** with automatic expiration
- **SQL Server** database with JDBC connectivity
- **Sample data** initialization

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring WebSocket
- Spring Data JPA
- SQL Server
- Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- SQL Server (2019 or later) running on localhost:1433
- Database: `SampleDB` (will be created automatically)
- SQL Server credentials: `sa` / `YourStrong@Passw0rd`

### SQL Server Setup

#### Option 1: Using Docker (Recommended)
```bash
# Start SQL Server using Docker Compose
docker-compose up -d

# Wait for SQL Server to be ready (about 30 seconds)
# The database and tables will be created automatically
```

#### Option 2: Manual SQL Server Installation
1. Install SQL Server 2019 or later
2. Run the setup script: `sql-server-setup.sql`
3. Ensure SQL Server is running on port 1433

### Running the Application

1. Clone or download the project
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. The application will start on `http://localhost:8080`

### Access Points

- **REST API**: `http://localhost:8080/api/`
- **Web Interface**: `http://localhost:8080/`
- **Database**: SQL Server on `localhost:1433`
  - Database Name: `SampleDB`
  - Username: `sa`
  - Password: `YourStrong@Passw0rd`

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

The application is configured to use SQL Server by default. To use a different database, update `application.properties`:

```properties
# Example for MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/bidding_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
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
