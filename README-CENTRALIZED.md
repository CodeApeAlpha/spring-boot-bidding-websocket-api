# Centralized Live Bidding System

This application now uses **Redis** as a centralized message broker to ensure live bid updates are shared across all application instances.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Instance 1    │    │   Instance 2    │    │   Instance N    │
│                 │    │                 │    │                 │
│  WebSocket      │    │  WebSocket      │    │  WebSocket      │
│  Clients        │    │  Clients        │    │  Clients        │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │        Redis              │
                    │   Centralized Broker      │
                    │                           │
                    │  Topics:                  │
                    │  - bidding:live-bids      │
                    │  - bidding:auctions       │
                    │  - bidding:item:{id}      │
                    └───────────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │        MySQL              │
                    │     Shared Database       │
                    └───────────────────────────┘
```

## 🚀 Quick Start

### 1. Start Infrastructure
```bash
# Start Redis and MySQL
docker-compose -f docker-compose-redis.yml up -d

# Or start just Redis
docker run -d --name bidding-redis -p 6379:6379 redis:7-alpine
```

### 2. Run Multiple Instances
```bash
# Terminal 1 - Instance 1
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"

# Terminal 2 - Instance 2  
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Terminal 3 - Instance 3
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

### 3. Test Centralized Messaging
1. Open browser tabs to `http://localhost:8080`, `http://localhost:8081`, `http://localhost:8082`
2. Place a bid on any instance
3. Watch the "Live Bids" section update on ALL instances simultaneously

## 🔧 Configuration

### Redis Settings
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
```

### Message Topics
- `bidding:live-bids` - All bid updates
- `bidding:auctions` - General auction updates  
- `bidding:item:{id}` - Item-specific updates

## 📊 Benefits

✅ **Centralized Data**: All instances share the same live bid feed
✅ **Scalable**: Add more instances without losing messages
✅ **Resilient**: Redis handles message persistence and delivery
✅ **Real-time**: Sub-millisecond message propagation
✅ **Load Balanced**: Distribute load across multiple instances

## 🧪 Testing

### Single Instance Test
```bash
# Start Redis
docker run -d --name bidding-redis -p 6379:6379 redis:7-alpine

# Start application
mvn spring-boot:run
```

### Multi-Instance Test
```bash
# Start infrastructure
docker-compose -f docker-compose-redis.yml up -d

# Start multiple instances
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080" &
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081" &
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082" &
```

## 🔍 Monitoring

Check Redis activity:
```bash
# Connect to Redis CLI
docker exec -it bidding-redis redis-cli

# Monitor all commands
MONITOR

# Check topics
PUBSUB CHANNELS bidding:*
```

## 🚨 Troubleshooting

### Redis Connection Issues
- Ensure Redis is running: `docker ps | grep redis`
- Check Redis logs: `docker logs bidding-redis`
- Verify port 6379 is accessible

### Message Not Propagating
- Check Redis connectivity in application logs
- Verify message listener is configured
- Test Redis pub/sub manually

### Performance Issues
- Monitor Redis memory usage
- Check connection pool settings
- Consider Redis clustering for high load
