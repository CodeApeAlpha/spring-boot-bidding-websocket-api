# Role-Based View Filtering Documentation

## Overview

The auction platform implements **role-based view filtering** to provide each user with a customized interface showing only the features and data relevant to their role. This creates a cleaner, more focused user experience and prevents confusion about permissions.

## Architecture

### Frontend Implementation
- **Location**: `src/main/resources/static/app.js`, `src/main/resources/static/index.html`
- **Mechanism**: JavaScript dynamically shows/hides sections based on user role
- **Trigger**: Executed on login, authentication check, and logout

### Key UI Sections
Each major section has a unique ID for programmatic control:

| Section ID | Description | Content |
|------------|-------------|---------|
| `placeBidSection` | Bid placement form | Item selector, bid amount input, submit button |
| `liveBidsSection` | Real-time bid feed | Live-updating list of all bids across auctions |
| `statsSection` | Statistics dashboard | Total bids, active auctions, highest bid |
| `auctionsSection` | Active auctions list | Grid of all active auction items |

---

## Role-Based Access Matrix

### Visual Access Control

| Section | BUYER | SELLER | ADMIN | Unauthenticated |
|---------|-------|--------|-------|-----------------|
| **Place Bid** | ✅ Visible & Enabled | ❌ Hidden | ❌ Hidden | ❌ Hidden |
| **Live Bids Feed** | ✅ Visible | ❌ Hidden | ✅ Visible (monitoring) | ❌ Hidden |
| **Stats Dashboard** | ✅ Visible | ✅ Visible | ✅ Visible | ❌ Hidden |
| **Active Auctions** | ✅ Visible | ✅ Visible | ✅ Visible | ❌ Hidden |

### Detailed Role Behavior

#### 🛒 BUYER Role
**Purpose**: Full bidding participant experience

**Visible Sections**:
- ✅ Place Bid Section (fully enabled)
  - Item selection dropdown populated with active auctions
  - Bid amount input field (enabled)
  - "Place Bid" button (enabled, primary style)
- ✅ Live Bids Feed
  - Real-time updates of all bids across platform
  - Shows bidder name, item, amount, timestamp
- ✅ Stats Dashboard
  - Total bids count
  - Active auctions count
  - Highest current bid
- ✅ Active Auctions Grid
  - All active auction items
  - Current prices and bid counts
  - Time remaining

**Welcome Message**: None (seamless access)

**Use Cases**:
- Browse available auctions
- Place bids on items
- Monitor competition in real-time
- Track personal bidding activity

---

#### 🏪 SELLER Role
**Purpose**: Auction management and monitoring (future: item creation)

**Visible Sections**:
- ❌ Place Bid Section (hidden entirely)
- ❌ Live Bids Feed (hidden)
- ✅ Stats Dashboard
  - Overview of platform activity
  - Monitor auction performance
- ✅ Active Auctions Grid
  - View all items (including own listings)
  - Monitor bid activity on items

**Welcome Message**: 
```
"Welcome Seller! You can manage your auction items here."
```
*Toast notification (info level)*

**Rationale**:
- Sellers should not bid on auctions (conflict of interest)
- Focus on managing their own listings
- Monitor overall platform health
- Future: Will see "Create Item" and "My Listings" sections

**Use Cases**:
- View auction performance
- Monitor bidding activity
- Prepare to manage own listings (future feature)

---

#### 🔧 ADMIN Role
**Purpose**: System monitoring, oversight, and user management

**Visible Sections**:
- ❌ Place Bid Section (hidden entirely)
- ✅ Live Bids Feed (visible for monitoring)
  - Observe all bidding activity
  - Detect suspicious patterns
  - System health monitoring
- ✅ Stats Dashboard
  - Platform-wide metrics
  - Activity overview
- ✅ Active Auctions Grid
  - Full system visibility
  - Monitor all auction items

**Welcome Message**: 
```
"Welcome Admin! System monitoring and user management access."
```
*Toast notification (info level)*

**Rationale**:
- Admins manage the platform, not participate in auctions
- Need full visibility for moderation and support
- Monitor system health and activity patterns
- Future: Will have "User Management" section

**Use Cases**:
- Monitor platform activity in real-time
- Detect anomalies or suspicious bidding
- Review auction performance
- System health checks
- User management (future feature)

---

#### 🔒 Unauthenticated Users
**Purpose**: Require authentication before access

**Visible Sections**:
- ❌ All sections hidden
- ✅ Login/Register buttons only

**Welcome Message**: 
```
"Please login to access the auction platform"
```
*Toast notification (info level)*

**Rationale**:
- Protect sensitive bidding data
- Ensure accountability (all actions tied to authenticated users)
- Encourage registration/login

**Available Actions**:
- Click "Login" button → Opens login modal
- Click "Register" button → Opens registration modal

---

## Implementation Details

### JavaScript Logic (`app.js`)

#### `setAuthenticatedUser(user, token)` Function

```javascript
function setAuthenticatedUser(user, token) {
    currentUser = user;
    authToken = token;
    
    // Update UI elements
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('userSection').style.display = 'flex';
    document.getElementById('userName').textContent = user.firstName + ' ' + user.lastName;
    
    // Set role badge
    const roleElement = document.getElementById('userRole');
    roleElement.textContent = user.role;
    roleElement.className = 'role-badge role-' + user.role.toLowerCase();
    
    // Get UI section references
    const placeBidSection = document.getElementById('placeBidSection');
    const liveBidsSection = document.getElementById('liveBidsSection');
    const statsSection = document.getElementById('statsSection');
    const auctionsSection = document.getElementById('auctionsSection');
    
    // Role-based configuration
    if (user.role === 'BUYER') {
        // Show all sections, enable bidding
        placeBidSection.style.display = 'block';
        liveBidsSection.style.display = 'block';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        // Enable bid controls
        document.getElementById('placeBidBtn').disabled = false;
        document.getElementById('bidAmount').disabled = false;
        document.getElementById('itemSelect').disabled = false;
        
        const bidBtn = document.getElementById('placeBidBtn');
        bidBtn.textContent = 'Place Bid';
        bidBtn.className = 'btn btn-primary';
        
        loadAllRecentBids();
        
    } else if (user.role === 'SELLER') {
        // Hide bidding sections
        placeBidSection.style.display = 'none';
        liveBidsSection.style.display = 'none';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        showToast('Welcome Seller! You can manage your auction items here.', 'info');
        
    } else if (user.role === 'ADMIN') {
        // Hide bidding, show monitoring
        placeBidSection.style.display = 'none';
        liveBidsSection.style.display = 'block';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        showToast('Welcome Admin! System monitoring and user management access.', 'info');
        
        loadAllRecentBids(); // Admin can monitor all activity
    }
}
```

#### `setUnauthenticatedUser()` Function

```javascript
function setUnauthenticatedUser() {
    currentUser = null;
    authToken = null;
    
    // Show login UI
    document.getElementById('authSection').style.display = 'flex';
    document.getElementById('userSection').style.display = 'none';
    
    // Hide all sections
    document.getElementById('placeBidSection').style.display = 'none';
    document.getElementById('liveBidsSection').style.display = 'none';
    document.getElementById('statsSection').style.display = 'none';
    document.getElementById('auctionsSection').style.display = 'none';
    
    showToast('Please login to access the auction platform', 'info');
}
```

### HTML Structure (`index.html`)

Each major section has a unique `id` attribute for JavaScript control:

```html
<!-- Sidebar -->
<aside class="sidebar">
    <!-- Place Bid Section -->
    <div id="placeBidSection" class="card">
        <div class="section-title">
            <h2><i class="fas fa-hammer"></i> Place a Bid</h2>
        </div>
        <!-- Bid form content -->
    </div>

    <!-- Live Bids Feed -->
    <div id="liveBidsSection" class="card">
        <div class="section-title">
            <h2><i class="fas fa-bolt"></i> Live Bids</h2>
            <div class="live-indicator">
                <div class="pulse"></div>
                <span>LIVE</span>
            </div>
        </div>
        <div id="bids" class="bids-list"></div>
    </div>
</aside>

<!-- Main Content Area -->
<section class="main-content">
    <!-- Stats Dashboard -->
    <div id="statsSection" class="stats-grid">
        <!-- Stats cards -->
    </div>

    <!-- Active Auctions -->
    <div id="auctionsSection" class="card">
        <div class="section-title">
            <h2><i class="fas fa-list"></i> Active Auctions</h2>
        </div>
        <div id="auctions" class="auction-grid"></div>
    </div>
</section>
```

---

## User Experience Flow

### 1. Initial Page Load (Unauthenticated)
```
User visits http://localhost:8080
  ↓
checkAuthStatus() executes
  ↓
No token found in localStorage
  ↓
setUnauthenticatedUser() called
  ↓
All sections hidden
  ↓
Login/Register buttons shown
  ↓
Toast: "Please login to access the auction platform"
```

### 2. Login Process
```
User clicks "Login" button
  ↓
Login modal opens
  ↓
User enters credentials
  ↓
POST /api/auth/login
  ↓
Receive JWT token + user details
  ↓
setAuthenticatedUser(user, token) called
  ↓
Role-based sections shown/hidden
  ↓
Welcome message displayed (if SELLER or ADMIN)
  ↓
Data loaded (items, bids, stats)
```

### 3. Registration Process
```
User clicks "Register" button
  ↓
Registration modal opens
  ↓
User enters details + selects role
  ↓
POST /api/auth/register
  ↓
Automatically logged in
  ↓
setAuthenticatedUser(user, token) called
  ↓
Role-based view configured
  ↓
Welcome message displayed
```

### 4. Logout Process
```
User clicks "Logout" button
  ↓
authToken and currentUser cleared
  ↓
localStorage.removeItem('token')
  ↓
setUnauthenticatedUser() called
  ↓
All sections hidden
  ↓
Redirect to login view
```

---

## Security Considerations

### Frontend Security
- **Visual only**: Frontend view filtering is for UX, not security
- **Backend enforcement**: All API endpoints have proper authorization
- **Token validation**: JWT required for authenticated endpoints
- **Role checking**: `@PreAuthorize` annotations on controller methods

### Backend Security Layer
Even if a user manipulates the frontend to show hidden sections, backend security prevents unauthorized actions:

| Action | Endpoint | Authorization |
|--------|----------|---------------|
| Place Bid | `POST /api/bids` | `@PreAuthorize("hasRole('BUYER')")` |
| View Bids | `GET /api/bids` | Public (read-only) |
| View Items | `GET /api/items` | Public (read-only) |
| User Management | `/api/admin/**` | `@PreAuthorize("hasRole('ADMIN')")` (future) |

**Example**: If a SELLER user somehow submits a bid:
```
Frontend: Hidden (should not be possible)
   ↓
If bypassed → POST /api/bids with JWT
   ↓
Backend: @PreAuthorize("hasRole('BUYER')") check
   ↓
Result: HTTP 403 Forbidden (Access Denied)
```

---

## Future Enhancements

### Planned Role-Specific Features

#### SELLER Enhancements
- **Create Auction Section**
  - Form to create new auction items
  - Image upload
  - Starting price, duration settings
- **My Listings Section**
  - View own active auctions
  - Edit/cancel listings
  - Bid history per item
- **Sales Analytics**
  - Total items sold
  - Revenue tracking
  - Performance metrics

#### ADMIN Enhancements
- **User Management Section**
  - View all users
  - Edit user roles
  - Enable/disable accounts
  - Ban users
- **Platform Analytics**
  - Total revenue
  - User growth
  - Auction success rates
- **Moderation Tools**
  - Flag suspicious bids
  - Review reported items
  - Audit logs

#### BUYER Enhancements
- **My Bids Section**
  - Active bids
  - Bid history
  - Won auctions
- **Watchlist**
  - Save favorite auctions
  - Price alerts
- **Notifications**
  - Outbid alerts
  - Auction ending reminders
  - Win notifications

---

## Testing Guide

### Manual Testing Steps

#### Test 1: BUYER Role
1. Visit `http://localhost:8080`
2. Click "Login"
3. Enter credentials: `buyer1` / `password123`
4. **Expected Results**:
   - ✅ Place Bid section visible and enabled
   - ✅ Live Bids feed visible
   - ✅ Stats dashboard visible
   - ✅ Active auctions grid visible
   - ✅ Can select items from dropdown
   - ✅ Can enter bid amount
   - ✅ "Place Bid" button enabled
   - ✅ Role badge shows "BUYER" (purple gradient)

#### Test 2: SELLER Role
1. Logout if logged in
2. Click "Login"
3. Enter credentials: `seller1` / `password123`
4. **Expected Results**:
   - ❌ Place Bid section hidden
   - ❌ Live Bids feed hidden
   - ✅ Stats dashboard visible
   - ✅ Active auctions grid visible
   - ✅ Toast: "Welcome Seller! You can manage your auction items here."
   - ✅ Role badge shows "SELLER" (pink gradient)

#### Test 3: ADMIN Role
1. Logout if logged in
2. Click "Login"
3. Enter credentials: `admin1` / `password123`
4. **Expected Results**:
   - ❌ Place Bid section hidden
   - ✅ Live Bids feed visible (monitoring)
   - ✅ Stats dashboard visible
   - ✅ Active auctions grid visible
   - ✅ Toast: "Welcome Admin! System monitoring and user management access."
   - ✅ Role badge shows "ADMIN" (orange gradient)

#### Test 4: Unauthenticated User
1. Logout if logged in
2. Visit `http://localhost:8080`
3. **Expected Results**:
   - ❌ All main sections hidden
   - ✅ Only login/register buttons visible
   - ✅ Toast: "Please login to access the auction platform"
   - ✅ Clean, empty main area

### Automated Test Scenarios

```javascript
// Test: BUYER sees all sections
describe('BUYER Role View', () => {
    it('should show all sections for BUYER role', () => {
        const user = { role: 'BUYER', firstName: 'John', lastName: 'Doe' };
        setAuthenticatedUser(user, 'fake-token');
        
        expect(document.getElementById('placeBidSection').style.display).toBe('block');
        expect(document.getElementById('liveBidsSection').style.display).toBe('block');
        expect(document.getElementById('statsSection').style.display).toBe('grid');
        expect(document.getElementById('auctionsSection').style.display).toBe('block');
    });
});

// Test: SELLER hides bidding sections
describe('SELLER Role View', () => {
    it('should hide bidding sections for SELLER role', () => {
        const user = { role: 'SELLER', firstName: 'Jane', lastName: 'Smith' };
        setAuthenticatedUser(user, 'fake-token');
        
        expect(document.getElementById('placeBidSection').style.display).toBe('none');
        expect(document.getElementById('liveBidsSection').style.display).toBe('none');
        expect(document.getElementById('statsSection').style.display).toBe('grid');
        expect(document.getElementById('auctionsSection').style.display).toBe('block');
    });
});

// Test: ADMIN shows monitoring view
describe('ADMIN Role View', () => {
    it('should show monitoring view for ADMIN role', () => {
        const user = { role: 'ADMIN', firstName: 'Admin', lastName: 'User' };
        setAuthenticatedUser(user, 'fake-token');
        
        expect(document.getElementById('placeBidSection').style.display).toBe('none');
        expect(document.getElementById('liveBidsSection').style.display).toBe('block');
        expect(document.getElementById('statsSection').style.display).toBe('grid');
        expect(document.getElementById('auctionsSection').style.display).toBe('block');
    });
});

// Test: Unauthenticated hides everything
describe('Unauthenticated User View', () => {
    it('should hide all sections for unauthenticated users', () => {
        setUnauthenticatedUser();
        
        expect(document.getElementById('placeBidSection').style.display).toBe('none');
        expect(document.getElementById('liveBidsSection').style.display).toBe('none');
        expect(document.getElementById('statsSection').style.display).toBe('none');
        expect(document.getElementById('auctionsSection').style.display).toBe('none');
    });
});
```

---

## Benefits

### 1. Improved User Experience
- **Reduced Cognitive Load**: Users only see relevant features
- **Clarity**: No confusion about what actions are permitted
- **Focus**: Each role has a purposeful, streamlined interface

### 2. Security Enhancement
- **Visual Feedback**: Clear indication of permissions
- **Reduced Attack Surface**: Hidden sections can't be easily manipulated
- **Aligned with Backend**: Frontend mirrors backend authorization

### 3. Maintainability
- **Modular Design**: Easy to add role-specific sections
- **Single Source of Truth**: Role logic in one function
- **Scalable**: Simple to add new roles or modify permissions

### 4. Professional Appearance
- **Polished UX**: Each user gets a tailored experience
- **Trust**: Users see a well-designed, purpose-built interface
- **Consistency**: Role behavior is predictable and documented

---

## Troubleshooting

### Issue: Sections not showing after login

**Symptoms**:
- User logs in successfully
- All sections remain hidden

**Possible Causes**:
1. JavaScript error in `setAuthenticatedUser()`
2. User object missing `role` property
3. CSS `display` style being overridden

**Solution**:
1. Open browser console (F12)
2. Check for JavaScript errors
3. Verify user object structure: `console.log(user)`
4. Check role value: `console.log(user.role)`
5. Inspect element display styles in DevTools

---

### Issue: Wrong sections visible for role

**Symptoms**:
- SELLER sees Place Bid section
- BUYER doesn't see Live Bids

**Possible Causes**:
1. Role string mismatch (e.g., "buyer" vs "BUYER")
2. Cached authentication data
3. Token containing wrong role

**Solution**:
1. Logout and clear localStorage: `localStorage.clear()`
2. Verify JWT payload at https://jwt.io
3. Check backend user role in database
4. Re-register user with correct role

---

### Issue: Backend rejects action despite UI allowing it

**Symptoms**:
- Button enabled and visible
- API returns 403 Forbidden

**Possible Causes**:
1. Frontend and backend roles out of sync
2. Token expired or invalid
3. Backend authorization rules changed

**Solution**:
1. Verify JWT token is valid and not expired
2. Check backend `@PreAuthorize` annotations
3. Review Spring Security configuration
4. Test backend endpoint with curl/Postman
5. Re-login to get fresh token

---

## Related Documentation

- **[USER_ROLES.md](USER_ROLES.md)** - Complete role definitions and permissions
- **[UI_UX_AUTHENTICATION_ALIGNMENT.md](UI_UX_AUTHENTICATION_ALIGNMENT.md)** - Authentication system alignment
- **[WEBSOCKET_FLOW.md](WEBSOCKET_FLOW.md)** - Real-time updates documentation
- **[LEAN-APPLICATION.md](LEAN-APPLICATION.md)** - Application architecture

---

## Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2025-10-01 | 1.0.0 | Initial implementation of role-based view filtering |

---

## Contact & Support

For questions or issues related to role-based views:
1. Review this documentation thoroughly
2. Check related docs in `/docs` folder
3. Test with different roles manually
4. Review browser console for JavaScript errors
5. Verify backend authorization is working correctly

**Test Accounts**:
- BUYER: `buyer1` / `password123`
- SELLER: `seller1` / `password123`
- ADMIN: `admin1` / `password123`

---

*Last Updated: October 1, 2025*
*Application Version: 1.0.0*
*Branch: feature/user-roles-buyer-seller-admin*

