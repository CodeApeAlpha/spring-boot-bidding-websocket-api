# User Roles & Authentication

## Overview

The bidding system supports three distinct user roles, each with specific permissions and capabilities:

1. **BUYER** - Can place bids on auction items
2. **SELLER** - Can create and manage auction items
3. **ADMIN** - Full system access with all capabilities

## Role Descriptions

### 🛒 BUYER Role
**Purpose**: Standard users who participate in auctions by placing bids

**Permissions**:
- ✅ View all auction items
- ✅ Place bids on active auctions
- ✅ View bid history
- ✅ View live bid updates via WebSocket
- ❌ Cannot create auction items
- ❌ Cannot access admin functions

**UI Features**:
- Full bidding interface enabled
- "Place Bid" button active
- Real-time bid notifications
- Role badge: Purple gradient (#667eea → #764ba2)

### 🏪 SELLER Role
**Purpose**: Users who create and manage auction items

**Permissions**:
- ✅ View all auction items
- ✅ Create new auction items (to be implemented)
- ✅ Manage their own auction items (to be implemented)
- ✅ View bid history on their items
- ❌ Cannot place bids on auctions
- ❌ Cannot access admin functions

**UI Features**:
- Bidding functionality disabled
- "Sellers Cannot Bid" message displayed
- Item creation/management interface (when implemented)
- Role badge: Pink gradient (#f093fb → #f5576c)

### 🔧 ADMIN Role
**Purpose**: System administrators with full access

**Permissions**:
- ✅ All BUYER permissions
- ✅ All SELLER permissions
- ✅ User management capabilities (to be implemented)
- ✅ System configuration access
- ✅ Full data access and modification

**UI Features**:
- All features enabled
- Admin dashboard (when implemented)
- User management interface (when implemented)
- Role badge: Orange gradient (#fad0c4 → #ff6a00)

---

## Creating Test Accounts

### ⚠️ Important Note
Test users **must be created via the registration API** to ensure passwords are properly BCrypt hashed. Direct SQL insertion will not work for authentication.

### Method 1: Via UI (Recommended)
1. Open http://localhost:8080
2. Click "Register"
3. Fill in the form and select desired role
4. Submit

### Method 2: Via API
Use the following commands to create test users:

#### Create BUYER Account
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "buyer1",
    "email": "buyer@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Buyer",
    "role": "BUYER"
  }'
```

#### Create SELLER Account
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seller1",
    "email": "seller@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Seller",
    "role": "SELLER"
  }'
```

#### Create ADMIN Account
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin1",
    "email": "admin@example.com",
    "password": "password123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

### Method 3: Automated Script
Run the provided script to create all test users at once:
```bash
# The script is automatically created and can be run with:
/tmp/create_test_users.sh
```

---

## Test Account Credentials

After creating via registration, use these credentials:

### 🟣 Buyer Account
- **Username**: `buyer1`
- **Email**: `buyer@example.com`
- **Password**: `password123`
- **Name**: John Buyer
- **Role**: BUYER

**Test Scenarios**:
- Login and place bids on active auctions
- View real-time bid updates
- Test bid validation (minimum amount, auction status)
- Verify bidding interface is fully functional

### 🟠 Seller Account
- **Username**: `seller1`
- **Email**: `seller@example.com`
- **Password**: `password123`
- **Name**: Jane Seller
- **Role**: SELLER

**Test Scenarios**:
- Login and verify bidding is disabled
- Confirm "Sellers Cannot Bid" message appears
- Create new auction items (when implemented)
- Manage auction items (when implemented)

### 🔴 Admin Account
- **Username**: `admin1`
- **Email**: `admin@example.com`
- **Password**: `password123`
- **Name**: Admin User
- **Role**: ADMIN

**Test Scenarios**:
- Login and access all system features
- Place bids with admin privileges
- Manage users (when implemented)
- Access admin dashboard (when implemented)

---

## Registration

New users can register through the UI with role selection:

1. Click **"Register"** button in the header
2. Fill in user details:
   - **First Name** (required)
   - **Last Name** (required)
   - **Username** (required, unique, 3-50 characters)
   - **Email** (required, unique, valid email format)
   - **Password** (required, minimum 6 characters)
3. **Select Account Type**:
   - **Buyer** - Place bids on auction items
   - **Seller** - Create and manage auctions
4. Submit registration

**Default Role**: If no role is specified, users are assigned the **BUYER** role by default.

---

## Authentication Flow

### Login Process
1. User enters username and password
2. Backend validates credentials using Spring Security
3. JWT token generated with user details and role
4. Token stored in browser localStorage
5. UI updated based on user role
6. Role-specific features enabled/disabled dynamically

### Token Structure
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "buyer1",
  "email": "buyer@example.com",
  "firstName": "John",
  "lastName": "Buyer",
  "role": "BUYER"
}
```

### Token Management
- **Duration**: 24 hours (86400000 milliseconds)
- **Storage**: Browser localStorage (`authToken` key)
- **Renewal**: Users must re-authenticate after expiration
- **Validation**: HS256 algorithm with secret key

---

## Security Configuration

### Endpoint Access Control

**Public Endpoints** (No authentication required):
```
GET  /api/items/**         - View auction items
GET  /api/bids/**          - View bids
POST /api/auth/register    - User registration
POST /api/auth/login       - User login
GET  /api/auth/me          - Current user (with valid token)
     /ws/**                - WebSocket connections
     /                     - Static UI resources
     /swagger-ui/**        - API documentation
```

**Protected Endpoints** (Authentication required):
```
POST /api/bids/**          - Place bids (BUYER, ADMIN)
POST /api/items/**         - Create items (SELLER, ADMIN) [to be implemented]
PUT  /api/items/**         - Update items (SELLER, ADMIN) [to be implemented]
DELETE /api/items/**       - Delete items (SELLER, ADMIN) [to be implemented]
     /api/admin/**         - Admin functions (ADMIN only) [to be implemented]
```

### Role-Based Access Control

Implemented using Spring Security's `@PreAuthorize` annotation:

```java
// Example: Only authenticated users can place bids
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BidResponse> placeBid(@Valid @RequestBody BidRequest bidRequest)

// Example: Only sellers and admins can create items
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest itemRequest)

// Example: Only admins can manage users
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<User>> getAllUsers()
```

### Security Features
- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing (strength 10)
- ✅ CSRF protection disabled (stateless API)
- ✅ CORS enabled for all origins
- ✅ Method-level security with `@PreAuthorize`
- ✅ HttpMethod-specific endpoint rules
- ✅ Session management: STATELESS

---

## Testing Guidelines

### Quick Test Commands

#### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer1","password":"password123"}'
```

#### Test Authenticated Bid
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer1","password":"password123"}' \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")

# Place bid
curl -X POST http://localhost:8080/api/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"itemId":1,"bidderName":"John Buyer","amount":600.00"}'
```

#### Test Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

### Valid Role Values
- `BUYER` (default)
- `SELLER`
- `ADMIN`

### Password Security
All passwords are hashed using BCrypt with strength 10:
```
BCrypt Hash Format: $2a$10$[22 character salt][31 character hash]
Example: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.
```

---

## Troubleshooting

### Common Issues

**Issue**: "Username is already taken" during registration
- **Solution**: Choose a different username or login with existing credentials

**Issue**: Login returns 500 error instead of 401
- **When**: Logging in with wrong password
- **Impact**: Low - error is still caught and displayed
- **Workaround**: None needed for users
- **Fix**: Backend error handling improvement (planned)

**Issue**: "User not found" during login
- **Solution**: Register first using the registration endpoint

**Issue**: Cannot place bid (401 Unauthorized)
- **Solution**: Ensure you're logged in and JWT token is included in Authorization header

**Issue**: Seller seeing "Sellers Cannot Bid" 
- **Solution**: This is expected behavior - sellers cannot bid, only buyers and admins can

---

## References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [BCrypt Password Hashing](https://en.wikipedia.org/wiki/Bcrypt)

---

**Last Updated**: October 1, 2025  
**Version**: 1.1  
**Branch**: feature/user-roles-buyer-seller-admin  
**Author**: Bidding System Development Team
