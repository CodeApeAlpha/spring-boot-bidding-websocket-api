/* Modern Bidding Platform with Authentication and View Switching */
let stompClient = null;
let currentItemId = null;
let currentBidSubscription = null;
let currentUser = null;
let authToken = null;

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
    connect(); // Connect to WebSocket immediately
    checkAuthStatus();
});

function initializeApp() {
    // Hide loading screen after a short delay
    setTimeout(() => {
        const loadingScreen = document.getElementById('loadingScreen');
        if (loadingScreen) {
            loadingScreen.classList.add('hidden');
        }
    }, 1000);
}

function setupEventListeners() {
    // Auth buttons
    document.getElementById('loginBtn').addEventListener('click', () => showModal('loginModal'));
    document.getElementById('registerBtn').addEventListener('click', () => showModal('registerModal'));
    document.getElementById('logoutBtn').addEventListener('click', logout);
    
    // Forms
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Refresh button
    const refreshBtn = document.getElementById('refreshAuctions');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadAuctions);
    }
    
    // Modal close on outside click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });
}

// View Switching Functions
function showLandingView() {
    document.body.classList.remove('app-mode');
    document.getElementById('landingView').style.display = 'block';
    document.getElementById('appView').style.display = 'none';
    document.getElementById('landingNavLinks').style.display = 'flex';
    document.getElementById('status').style.display = 'none';
}

function showAppView() {
    document.body.classList.add('app-mode');
    document.getElementById('landingView').style.display = 'none';
    document.getElementById('appView').style.display = 'block';
    document.getElementById('landingNavLinks').style.display = 'none';
    document.getElementById('status').style.display = 'flex';
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Authentication Functions
async function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    if (token) {
        try {
            const response = await fetch('/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const user = await response.json();
                setAuthenticatedUser(user, token);
            } else {
                localStorage.removeItem('authToken');
                setUnauthenticatedUser();
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            localStorage.removeItem('authToken');
            setUnauthenticatedUser();
        }
    } else {
        setUnauthenticatedUser();
    }
}

function setAuthenticatedUser(user, token) {
    currentUser = user;
    authToken = token;
    
    // Update UI
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('userSection').style.display = 'flex';
    document.getElementById('userName').textContent = user.firstName + ' ' + user.lastName;
    
    // Set role badge with styling
    const roleElement = document.getElementById('userRole');
    roleElement.textContent = user.role;
    roleElement.className = 'role-badge role-' + user.role.toLowerCase();
    
    // Switch to app view
    showAppView();
    
    // Get UI sections
    const placeBidSection = document.getElementById('placeBidSection');
    const liveBidsSection = document.getElementById('liveBidsSection');
    const statsSection = document.getElementById('statsSection');
    const auctionsSection = document.getElementById('auctionsSection');
    
    // Configure UI based on role
    if (user.role === 'BUYER') {
        // BUYER: Show all sections, enable bidding
        placeBidSection.style.display = 'block';
        liveBidsSection.style.display = 'block';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        document.getElementById('placeBidBtn').disabled = false;
        document.getElementById('bidAmount').disabled = false;
        document.getElementById('itemSelect').disabled = false;
        
        loadAllRecentBids();
        
    } else if (user.role === 'SELLER') {
        // SELLER: Show auctions and stats, hide bidding
        placeBidSection.style.display = 'none';
        liveBidsSection.style.display = 'none';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        showToast('Welcome Seller! You can manage your auction items here.', 'info');
        
    } else if (user.role === 'ADMIN') {
        // ADMIN: Hide regular layout, show admin dashboard
        const regularLayout = document.getElementById('regularAppLayout');
        if (regularLayout) {
            regularLayout.style.display = 'none';
        }
        
        // Show admin dashboard
        const adminDashboard = document.getElementById('adminDashboard');
        if (adminDashboard) {
            adminDashboard.style.display = 'block';
            
            // Set admin username
            const adminUsernameElem = document.getElementById('adminUsername');
            if (adminUsernameElem) {
                adminUsernameElem.textContent = user.username;
            }
            
            loadAdminData();
        }
        
        showToast('Welcome Admin! Full system management access.', 'info');
        
        // Don't load regular buyer/seller data
        return;
    }
    
    // Load auction data
    loadAuctions();
    updateStats();
}

function setUnauthenticatedUser() {
    currentUser = null;
    authToken = null;
    
    document.getElementById('authSection').style.display = 'flex';
    document.getElementById('userSection').style.display = 'none';
    
    // Hide admin dashboard and show regular layout
    const adminDashboard = document.getElementById('adminDashboard');
    const regularLayout = document.getElementById('regularAppLayout');
    if (adminDashboard) {
        adminDashboard.style.display = 'none';
    }
    if (regularLayout) {
        regularLayout.style.display = 'grid';
    }
    
    // Show landing view
    showLandingView();
}

async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const authResponse = await response.json();
            localStorage.setItem('authToken', authResponse.token);
            setAuthenticatedUser(authResponse, authResponse.token);
            closeModal('loginModal');
            showToast('Welcome back to BidHub!', 'success');
        } else {
            const error = await response.json();
            showToast(error.message || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('Login failed. Please try again.', 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const formData = {
        username: document.getElementById('registerUsername').value,
        email: document.getElementById('registerEmail').value,
        password: document.getElementById('registerPassword').value,
        firstName: document.getElementById('registerFirstName').value,
        lastName: document.getElementById('registerLastName').value,
        role: document.getElementById('registerRole').value
    };
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (response.ok) {
            const authResponse = await response.json();
            localStorage.setItem('authToken', authResponse.token);
            setAuthenticatedUser(authResponse, authResponse.token);
            closeModal('registerModal');
            showToast('Welcome to BidHub! Your account has been created.', 'success');
        } else {
            const error = await response.json();
            showToast(error.message || 'Registration failed', 'error');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showToast('Registration failed. Please try again.', 'error');
    }
}

function logout() {
    localStorage.removeItem('authToken');
    setUnauthenticatedUser();
    showToast('You have been logged out. See you soon!', 'info');
}

// WebSocket Functions
function setStatus(connected) {
  const el = document.getElementById('status');
    if (!el) return;
    
    const icon = el.querySelector('i');
    const text = el.querySelector('span');
    
    if (connected) {
        el.className = 'status connected';
        icon.className = 'fas fa-circle';
        text.textContent = 'Connected';
    } else {
        el.className = 'status disconnected';
        icon.className = 'fas fa-circle';
        text.textContent = 'Disconnected';
    }
}

function connect() {
    if (stompClient && stompClient.connected) {
        return;
    }
    
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
    setStatus(true);
        console.log('Connected: ' + frame);

        // Subscribe to general auction updates
    stompClient.subscribe('/topic/auctions', function (message) {
      const payload = JSON.parse(message.body);
            console.log('Received auction update:', payload);
            if (payload.type === 'BID_UPDATE' || payload.type === 'NEW_BID') {
                // Add to landing preview for visitors
                if (!currentUser) {
                    addBidToLandingPreview(payload.data);
                } else {
                    // Add to app view for authenticated users
                addBidToSharedFeed(payload.data);
                }
                updateAuctionDisplay(payload.data);
                updateStats();
            } else if (payload.type === 'AUCTION_UPDATE') {
                updateAuctionDisplay(payload.data);
            }
        });

        // Load initial data
        if (currentUser) {
            // Authenticated users
        loadAuctions();
        updateStats();
            loadAllRecentBids();
        } else {
            // Visitors - load preview bids
            loadPreviewBids();
        }
    }, function(error) {
        console.error('STOMP error: ' + error);
        setStatus(false);
        // Retry connection after 5 seconds
        setTimeout(connect, 5000);
  });
}

function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
    }
  setStatus(false);
}

// API Functions
async function loadAuctions() {
    try {
        const response = await fetch('/api/items/active');
        const items = await response.json();
        displayAuctions(items);
        populateItemSelect(items);
        updateStats();
    } catch (error) {
        console.error('Error loading auctions:', error);
        showToast('Failed to load auctions', 'error');
    }
}

function displayAuctions(auctions) {
  const container = document.getElementById('auctions');
    if (!container) return;
    
  container.innerHTML = '';
    
  auctions.forEach(auction => {
    const div = document.createElement('div');
        div.className = 'auction-item fade-in';
    div.innerHTML = `
      <h3>${auction.name}</h3>
      <p class="meta">${auction.description || ''}</p>
            <div class="price">$${auction.currentHighestBid.toFixed(2)}</div>
            <div class="time">Ends: ${new Date(auction.endTime).toLocaleString()}</div>
      <div class="row">
                <button class="btn btn-outline btn-sm" onclick="loadBids(${auction.id})">
                    <i class="fas fa-eye"></i>
                    View Bids
                </button>
      </div>
    `;
    container.appendChild(div);
  });
}

function populateItemSelect(auctions) {
  const select = document.getElementById('itemSelect');
    if (!select) return;
    
    select.innerHTML = '<option value="">Choose an item...</option>';
  auctions.forEach(a => {
    const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = a.name;
        select.appendChild(opt);
    });
}

async function placeBid() {
    // Check authentication first
    if (!currentUser) {
        showToast('Please login to place a bid', 'warning');
        showModal('loginModal');
        return;
    }
    
  const itemId = document.getElementById('itemSelect').value;
  const amount = document.getElementById('bidAmount').value;
    
    if (!itemId || !amount) {
        showToast('Please select an item and enter a bid amount', 'warning');
        return;
    }
    
    const dto = { 
        itemId: parseInt(itemId), 
        bidderName: currentUser.firstName + ' ' + currentUser.lastName, 
        amount: parseFloat(amount) 
    };
    
    try {
        const response = await fetch('/api/bids', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(dto)
        });
        
        if (response.ok) {
            document.getElementById('bidAmount').value = '';
            showToast('Bid placed successfully!', 'success');
            loadBids(itemId);
        } else {
            const error = await response.json();
            showToast(error.message || 'Failed to place bid', 'error');
        }
    } catch (error) {
        console.error('Error placing bid:', error);
        showToast('Failed to place bid. Please try again.', 'error');
    }
}

async function loadBids(itemId) {
    if (!currentUser) {
        showToast('Please login to view bids', 'warning');
        showModal('loginModal');
        return;
    }
    
  currentItemId = parseInt(itemId);
    try {
        const response = await fetch(`/api/bids/item/${itemId}`);
        const bids = await response.json();
        displayBids(bids);
    } catch (error) {
        console.error('Error loading bids:', error);
        showToast('Failed to load bids', 'error');
    }
}

function displayBids(bids) {
  const container = document.getElementById('bids');
    if (!container) return;
    
  container.innerHTML = '';
  bids.forEach(bid => addBidToHtml(container, bid, false));
}

async function loadAllRecentBids() {
    if (!currentUser) {
        return;
    }
    
    try {
        const response = await fetch('/api/bids');
        const bids = await response.json();
        const container = document.getElementById('bids');
        if (!container) return;
        
        container.innerHTML = '';
        
        // Sort by timestamp descending (newest first)
        bids.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        bids.forEach(bid => addBidToHtml(container, bid, false));
    } catch (error) {
        console.error('Error loading all bids:', error);
        showToast('Failed to load recent bids', 'error');
    }
}

function addBidToHtml(container, bid, prepend = true) {
  const div = document.createElement('div');
  div.className = 'bid-item' + (bid.isWinning ? ' winning-bid' : '');
  div.innerHTML = `
        <div>
            <div class="bid-amount">$${bid.amount.toFixed(2)}</div>
            <div class="bid-bidder">by ${bid.bidderName}</div>
            <div class="bid-item-id">Item ID: ${bid.itemId}</div>
        </div>
        <div class="bid-time">${new Date(bid.timestamp).toLocaleTimeString()}</div>
    `;
    
    if (prepend) {
        container.insertBefore(div, container.firstChild);
    } else {
        container.appendChild(div);
    }
}

function addBidToSharedFeed(bid) {
  const container = document.getElementById('bids');
    if (!container) return;
  addBidToHtml(container, bid, true);
}

function updateAuctionDisplay(bid) {
    const auctions = document.querySelectorAll('.auction-item');
    auctions.forEach(auction => {
        const button = auction.querySelector('button');
        if (button && button.onclick) {
            const auctionId = button.onclick.toString().match(/loadBids\((\d+)\)/);
            if (auctionId && parseInt(auctionId[1]) === bid.itemId) {
                const priceElement = auction.querySelector('.price');
                if (priceElement) {
                    priceElement.textContent = `$${bid.amount.toFixed(2)}`;
                }
            }
        }
    });
}

async function updateStats() {
    try {
        const [bidsResponse, itemsResponse] = await Promise.all([
            fetch('/api/bids'),
            fetch('/api/items/active')
        ]);
        
        const bids = await bidsResponse.json();
        const items = await itemsResponse.json();
        
        const totalBidsEl = document.getElementById('totalBids');
        const activeAuctionsEl = document.getElementById('activeAuctions');
        const highestBidEl = document.getElementById('highestBid');
        
        if (totalBidsEl) totalBidsEl.textContent = bids.length;
        if (activeAuctionsEl) activeAuctionsEl.textContent = items.length;
        
        const highestBid = Math.max(...bids.map(b => b.amount), 0);
        if (highestBidEl) highestBidEl.textContent = `$${highestBid.toFixed(2)}`;
    } catch (error) {
        console.error('Error updating stats:', error);
    }
}

// Modal Functions
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('show');
    document.body.style.overflow = 'auto';
    
    // Clear form
    const form = modal.querySelector('form');
    if (form) {
        form.reset();
    }
}

// Toast Notification Functions
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = getToastIcon(type);
    toast.innerHTML = `
        <i class="${icon}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
}

function getToastIcon(type) {
    const icons = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };
    return icons[type] || icons.info;
}

// Live Bids Preview Functions (for visitors)
async function loadPreviewBids() {
    const container = document.getElementById('liveBidsPreviewContainer');
    if (!container) return;
    
    try {
        const response = await fetch('/api/bids');
        const bids = await response.json();
        
        // Remove placeholder
        container.innerHTML = '';
        
        // Sort by timestamp descending (newest first) and take last 6
        bids.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        const recentBids = bids.slice(0, 6);
        
        if (recentBids.length === 0) {
            container.innerHTML = `
                <div class="preview-bid-placeholder">
                    <i class="fas fa-gavel"></i>
                    <p>Be the first to bid today!</p>
                </div>
            `;
            return;
        }
        
        recentBids.forEach(bid => {
            const bidElement = createPreviewBidElement(bid, false);
            container.appendChild(bidElement);
        });
    } catch (error) {
        console.error('Error loading preview bids:', error);
        container.innerHTML = `
            <div class="preview-bid-placeholder">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Unable to load bids. Reconnecting...</p>
            </div>
        `;
    }
}

function addBidToLandingPreview(bid) {
    const container = document.getElementById('liveBidsPreviewContainer');
    if (!container) return;
    
    // Remove placeholder if exists
    const placeholder = container.querySelector('.preview-bid-placeholder');
    if (placeholder) {
        placeholder.remove();
    }
    
    // Create and add new bid element
    const bidElement = createPreviewBidElement(bid, true);
    container.insertBefore(bidElement, container.firstChild);
    
    // Keep only last 6 bids
    const allBids = container.querySelectorAll('.preview-bid-item');
    if (allBids.length > 6) {
        allBids[allBids.length - 1].remove();
    }
    
    // Remove 'new' badge after 3 seconds
    setTimeout(() => {
        bidElement.classList.remove('preview-bid-new');
    }, 3000);
}

function createPreviewBidElement(bid, isNew) {
    const div = document.createElement('div');
    div.className = 'preview-bid-item' + (isNew ? ' preview-bid-new' : '');
    
    // Get item name (if available) or use item ID
    const itemName = bid.itemName || `Auction Item #${bid.itemId}`;
    
    div.innerHTML = `
        <div class="preview-bid-header">
            ${isNew ? '<span class="preview-bid-badge">🔴 NEW BID</span>' : '<span class="preview-bid-badge">💎 BID</span>'}
            <span class="preview-bid-time">${getRelativeTime(bid.timestamp)}</span>
        </div>
        <div class="preview-bid-content">
            <div class="preview-bid-item-name">${itemName}</div>
            <div class="preview-bid-amount">$${bid.amount.toFixed(2)}</div>
            <div class="preview-bid-bidder">👤 ${anonymizeBidder(bid.bidderName)}</div>
        </div>
    `;
    
    return div;
}

function anonymizeBidder(fullName) {
    if (!fullName) return 'Anonymous';
    
    const parts = fullName.trim().split(' ');
    if (parts.length > 1) {
        // "John Doe" -> "John D."
        return `${parts[0]} ${parts[1].charAt(0)}.`;
    }
    // Single name
    return parts[0];
}

function getRelativeTime(timestamp) {
    const now = new Date();
    const bidTime = new Date(timestamp);
    const diffInSeconds = Math.floor((now - bidTime) / 1000);
    
    if (diffInSeconds < 10) {
        return 'just now';
    } else if (diffInSeconds < 60) {
        return `${diffInSeconds} seconds ago`;
    } else if (diffInSeconds < 3600) {
        const minutes = Math.floor(diffInSeconds / 60);
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
        const hours = Math.floor(diffInSeconds / 3600);
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else {
        const days = Math.floor(diffInSeconds / 86400);
        return `${days} day${days > 1 ? 's' : ''} ago`;
    }
}

// Admin Dashboard Functions
function loadAdminData() {
    // Load overview by default
    loadOverviewStats();
}

async function refreshUsers() {
    const container = document.getElementById('usersTable');
    if (!container) return;
    
    try {
        const response = await fetch('/api/admin/users', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load users');
        }
        
        const users = await response.json();
        displayUsersTable(users);
    } catch (error) {
        console.error('Error loading users:', error);
        container.innerHTML = '<p class="text-danger">Failed to load users. ' + error.message + '</p>';
    }
}

function displayUsersTable(users) {
    const container = document.getElementById('usersTable');
    if (!container || !users || users.length === 0) {
        container.innerHTML = '<p>No users found.</p>';
        return;
    }
    
    const table = `
        <table class="admin-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(user => `
                    <tr>
                        <td>${user.id}</td>
                        <td><strong>${user.username}</strong></td>
                        <td>${user.firstName} ${user.lastName}</td>
                        <td>${user.email}</td>
                        <td><span class="user-role-badge role-${user.role.toLowerCase()}">${user.role}</span></td>
                        <td><span class="user-status ${user.enabled ? 'active' : 'disabled'}">
                            ${user.enabled ? '✓ Active' : '✗ Disabled'}
                        </span></td>
                        <td>${new Date(user.createdAt).toLocaleDateString()}</td>
                        <td>
                            <div class="admin-actions">
                                <button class="btn-admin-action btn-edit" onclick="editUserRole(${user.id}, '${user.role}')" title="Change Role">
                                    <i class="fas fa-user-tag"></i>
                                </button>
                                <button class="btn-admin-action btn-toggle" onclick="toggleUserStatus(${user.id}, ${user.enabled})" title="Toggle Status">
                                    <i class="fas fa-power-off"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    
    container.innerHTML = table;
}

async function editUserRole(userId, currentRole) {
    const roles = ['BUYER', 'SELLER', 'ADMIN'];
    const roleOptions = roles.map(r => `${r === currentRole ? '➤ ' : ''}${r}`).join('\n');
    
    const newRole = prompt(`Change user role:\n\n${roleOptions}\n\nEnter new role (BUYER, SELLER, or ADMIN):`, currentRole);
    
    if (!newRole || newRole.toUpperCase() === currentRole) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/users/${userId}/role`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ role: newRole.toUpperCase() })
        });
        
        if (response.ok) {
            showToast('User role updated successfully!', 'success');
            refreshUsers();
        } else {
            showToast('Failed to update user role', 'error');
        }
    } catch (error) {
        console.error('Error updating user role:', error);
        showToast('Error updating user role', 'error');
    }
}

async function toggleUserStatus(userId, currentStatus) {
    const action = currentStatus ? 'disable' : 'enable';
    if (!confirm(`Are you sure you want to ${action} this user?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/users/${userId}/toggle-status`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            showToast(`User ${action}d successfully!`, 'success');
            refreshUsers();
        } else {
            showToast(`Failed to ${action} user`, 'error');
        }
    } catch (error) {
        console.error('Error toggling user status:', error);
        showToast('Error toggling user status', 'error');
    }
}

async function refreshAdminStats() {
    try {
        const response = await fetch('/api/admin/stats', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load statistics');
        }
        
        const stats = await response.json();
        return stats;
    } catch (error) {
        console.error('Error loading stats:', error);
        return null;
    }
}

function displayAdminStats(stats, containerId = 'adminStatsGrid') {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = `
        <div class="admin-stat-card">
            <h4><i class="fas fa-users"></i> Total Users</h4>
            <div class="stat-value">${stats.totalUsers}</div>
            <div class="stat-label">Registered accounts</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-shopping-cart"></i> Buyers</h4>
            <div class="stat-value">${stats.buyerCount}</div>
            <div class="stat-label">Active bidders</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-store"></i> Sellers</h4>
            <div class="stat-value">${stats.sellerCount}</div>
            <div class="stat-label">Item creators</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-user-shield"></i> Admins</h4>
            <div class="stat-value">${stats.adminCount}</div>
            <div class="stat-label">System administrators</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-check-circle"></i> Active Users</h4>
            <div class="stat-value">${stats.activeUsers}</div>
            <div class="stat-label">Enabled accounts</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-ban"></i> Disabled Users</h4>
            <div class="stat-value">${stats.disabledUsers}</div>
            <div class="stat-label">Inactive accounts</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-box"></i> Total Auctions</h4>
            <div class="stat-value">${stats.totalAuctions}</div>
            <div class="stat-label">All time items</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-fire"></i> Active Auctions</h4>
            <div class="stat-value">${stats.activeAuctions}</div>
            <div class="stat-label">Currently bidding</div>
        </div>
        <div class="admin-stat-card">
            <h4><i class="fas fa-gavel"></i> Total Bids</h4>
            <div class="stat-value">${stats.totalBids}</div>
            <div class="stat-label">Platform-wide</div>
        </div>
    `;
}

function showAdminSection(event, sectionName) {
    // Prevent default link behavior
    if (event) {
        event.preventDefault();
    }
    
    // Hide all sections
    document.querySelectorAll('.admin-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Remove active class from all nav items
    document.querySelectorAll('.admin-nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Show selected section
    const section = document.getElementById(`adminSection${sectionName.charAt(0).toUpperCase() + sectionName.slice(1)}`);
    if (section) {
        section.classList.add('active');
    }
    
    // Add active class to selected nav item
    if (event && event.target) {
        const navItem = event.target.closest('.admin-nav-item');
        if (navItem) {
            navItem.classList.add('active');
        }
    }
    
    // Load data for the selected section
    if (sectionName === 'overview') {
        loadOverviewStats();
    } else if (sectionName === 'users') {
        refreshUsers();
    } else if (sectionName === 'analytics') {
        loadAnalyticsStats();
    } else if (sectionName === 'auctions') {
        loadAdminAuctions();
    } else if (sectionName === 'activity') {
        loadAdminActivity();
    }
}

function loadOverviewStats() {
    refreshAdminStats().then(stats => {
        if (stats) {
            displayAdminStats(stats, 'overviewStatsGrid');
        }
    });
}

function loadAnalyticsStats() {
    refreshAdminStats().then(stats => {
        if (stats) {
            displayAdminStats(stats, 'analyticsStatsGrid');
        }
    });
}

// Auctions data storage
let auctionsData = {
    items: [],
    bids: [],
    bidCounts: {},
    currentPage: 1,
    itemsPerPage: 12,
    searchTerm: '',
    filter: 'all',
    sortBy: 'bids'
};

function loadAdminAuctions() {
    const container = document.getElementById('adminAuctionsView');
    if (!container) return;
    
    // Show loading state
    container.innerHTML = '<div class="admin-loading"><i class="fas fa-spinner fa-spin"></i> Loading auctions...</div>';
    
    // Fetch auctions and bids
    Promise.all([
        fetch('/api/items').then(r => r.json()),
        fetch('/api/bids').then(r => r.json())
    ])
    .then(([items, bids]) => {
        if (!items || items.length === 0) {
            container.innerHTML = `
                <div class="admin-empty-state">
                    <i class="fas fa-gavel"></i>
                    <h3>No Auctions Yet</h3>
                    <p>Auction items will appear here once sellers create them.</p>
                </div>
            `;
            return;
        }
        
        // Store data
        auctionsData.items = items;
        auctionsData.bids = bids;
        
        // Calculate bid counts
        auctionsData.bidCounts = {};
        bids.forEach(bid => {
            auctionsData.bidCounts[bid.itemId] = (auctionsData.bidCounts[bid.itemId] || 0) + 1;
        });
        
        // Setup event listeners
        setupAuctionControls();
        
        // Display auctions
        displayAuctions();
    })
    .catch(error => {
        console.error('Error loading auctions:', error);
        container.innerHTML = `
            <div class="admin-error-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Failed to Load Auctions</h3>
                <p>${error.message}</p>
                <button class="btn btn-primary" onclick="loadAdminAuctions()">
                    <i class="fas fa-sync-alt"></i> Retry
                </button>
            </div>
        `;
    });
}

function setupAuctionControls() {
    // Search
    const searchInput = document.getElementById('auctionSearch');
    if (searchInput && !searchInput.dataset.initialized) {
        searchInput.dataset.initialized = 'true';
        searchInput.addEventListener('input', (e) => {
            auctionsData.searchTerm = e.target.value.toLowerCase();
            auctionsData.currentPage = 1;
            displayAuctions();
        });
    }
    
    // Filter
    const filterSelect = document.getElementById('auctionFilter');
    if (filterSelect && !filterSelect.dataset.initialized) {
        filterSelect.dataset.initialized = 'true';
        filterSelect.addEventListener('change', (e) => {
            auctionsData.filter = e.target.value;
            auctionsData.currentPage = 1;
            displayAuctions();
        });
    }
    
    // Sort
    const sortSelect = document.getElementById('auctionSort');
    if (sortSelect && !sortSelect.dataset.initialized) {
        sortSelect.dataset.initialized = 'true';
        sortSelect.addEventListener('change', (e) => {
            auctionsData.sortBy = e.target.value;
            displayAuctions();
        });
    }
    
    // Items per page
    const itemsPerPageSelect = document.getElementById('itemsPerPage');
    if (itemsPerPageSelect && !itemsPerPageSelect.dataset.initialized) {
        itemsPerPageSelect.dataset.initialized = 'true';
        itemsPerPageSelect.addEventListener('change', (e) => {
            auctionsData.itemsPerPage = e.target.value === 'all' ? 999999 : parseInt(e.target.value);
            auctionsData.currentPage = 1;
            displayAuctions();
        });
    }
}

function displayAuctions() {
    const container = document.getElementById('adminAuctionsView');
    if (!container) return;
    
    let filtered = [...auctionsData.items];
    
    // Apply search filter
    if (auctionsData.searchTerm) {
        filtered = filtered.filter(item => 
            item.name.toLowerCase().includes(auctionsData.searchTerm) ||
            (item.description && item.description.toLowerCase().includes(auctionsData.searchTerm))
        );
    }
    
    // Apply status filter
    if (auctionsData.filter === 'active') {
        filtered = filtered.filter(item => item.active !== false);
    } else if (auctionsData.filter === 'inactive') {
        filtered = filtered.filter(item => item.active === false);
    } else if (auctionsData.filter === 'no-bids') {
        filtered = filtered.filter(item => (auctionsData.bidCounts[item.id] || 0) === 0);
    } else if (auctionsData.filter === 'with-bids') {
        filtered = filtered.filter(item => (auctionsData.bidCounts[item.id] || 0) > 0);
    }
    
    // Apply sorting
    filtered.sort((a, b) => {
        const bidCountA = auctionsData.bidCounts[a.id] || 0;
        const bidCountB = auctionsData.bidCounts[b.id] || 0;
        const priceA = a.currentPrice || a.startingPrice;
        const priceB = b.currentPrice || b.startingPrice;
        
        switch (auctionsData.sortBy) {
            case 'bids':
                return bidCountB - bidCountA;
            case 'price-high':
                return priceB - priceA;
            case 'price-low':
                return priceA - priceB;
            case 'name':
                return a.name.localeCompare(b.name);
            default:
                return 0;
        }
    });
    
    // Update results info
    updateResultsInfo(filtered.length, auctionsData.items.length);
    
    // Check if empty after filtering
    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="admin-empty-state">
                <i class="fas fa-search"></i>
                <h3>No Auctions Found</h3>
                <p>Try adjusting your search or filter criteria.</p>
            </div>
        `;
        document.getElementById('auctionsPagination').innerHTML = '';
        return;
    }
    
    // Pagination
    const totalPages = Math.ceil(filtered.length / auctionsData.itemsPerPage);
    const startIndex = (auctionsData.currentPage - 1) * auctionsData.itemsPerPage;
    const endIndex = startIndex + auctionsData.itemsPerPage;
    const paginatedItems = filtered.slice(startIndex, endIndex);
    
    // Render cards
    container.innerHTML = paginatedItems.map(item => {
        const bidCount = auctionsData.bidCounts[item.id] || 0;
        const currentPrice = item.currentPrice || item.startingPrice;
        const isActive = item.active !== false;
        
        return `
            <div class="admin-auction-card ${isActive ? '' : 'inactive'}">
                <div class="admin-auction-header">
                    <div class="admin-auction-badge ${isActive ? 'badge-active' : 'badge-inactive'}">
                        ${isActive ? '<i class="fas fa-circle"></i> Active' : '<i class="fas fa-circle"></i> Inactive'}
                    </div>
                    <div class="admin-auction-id">ID: ${item.id}</div>
                </div>
                
                <div class="admin-auction-body">
                    <h3 class="admin-auction-title">${item.name}</h3>
                    <p class="admin-auction-description">${item.description || 'No description'}</p>
                    
                    <div class="admin-auction-stats">
                        <div class="admin-auction-stat">
                            <div class="stat-icon"><i class="fas fa-dollar-sign"></i></div>
                            <div class="stat-details">
                                <span class="stat-value">$${currentPrice.toFixed(2)}</span>
                                <span class="stat-label">Current Price</span>
                            </div>
                        </div>
                        <div class="admin-auction-stat">
                            <div class="stat-icon"><i class="fas fa-tag"></i></div>
                            <div class="stat-details">
                                <span class="stat-value">$${item.startingPrice.toFixed(2)}</span>
                                <span class="stat-label">Starting Price</span>
                            </div>
                        </div>
                        <div class="admin-auction-stat">
                            <div class="stat-icon"><i class="fas fa-gavel"></i></div>
                            <div class="stat-details">
                                <span class="stat-value">${bidCount}</span>
                                <span class="stat-label">Total Bids</span>
                            </div>
                        </div>
                    </div>
                    
                    ${bidCount > 0 ? `
                        <div class="admin-auction-activity">
                            <i class="fas fa-fire"></i>
                            <span>${bidCount} ${bidCount === 1 ? 'bid' : 'bids'} placed</span>
                        </div>
                    ` : `
                        <div class="admin-auction-activity inactive">
                            <i class="fas fa-clock"></i>
                            <span>No bids yet</span>
                        </div>
                    `}
                </div>
            </div>
        `;
    }).join('');
    
    // Render pagination
    renderPagination(totalPages);
}

function updateResultsInfo(showing, total) {
    const infoElement = document.getElementById('auctionsResultsInfo');
    if (!infoElement) return;
    
    if (showing === total) {
        infoElement.innerHTML = `<span>Showing <strong>${total}</strong> ${total === 1 ? 'auction' : 'auctions'}</span>`;
    } else {
        infoElement.innerHTML = `<span>Showing <strong>${showing}</strong> of <strong>${total}</strong> auctions</span>`;
    }
}

function renderPagination(totalPages) {
    const paginationElement = document.getElementById('auctionsPagination');
    if (!paginationElement || totalPages <= 1) {
        paginationElement.innerHTML = '';
        return;
    }
    
    const currentPage = auctionsData.currentPage;
    let paginationHTML = '<div class="pagination-buttons">';
    
    // Previous button
    paginationHTML += `
        <button class="pagination-btn ${currentPage === 1 ? 'disabled' : ''}" 
                onclick="goToPage(${currentPage - 1})" 
                ${currentPage === 1 ? 'disabled' : ''}>
            <i class="fas fa-chevron-left"></i> Previous
        </button>
    `;
    
    // Page numbers
    paginationHTML += '<div class="pagination-numbers">';
    
    // Always show first page
    if (currentPage > 3) {
        paginationHTML += `<button class="pagination-num" onclick="goToPage(1)">1</button>`;
        if (currentPage > 4) {
            paginationHTML += '<span class="pagination-ellipsis">...</span>';
        }
    }
    
    // Show pages around current
    for (let i = Math.max(1, currentPage - 2); i <= Math.min(totalPages, currentPage + 2); i++) {
        paginationHTML += `
            <button class="pagination-num ${i === currentPage ? 'active' : ''}" 
                    onclick="goToPage(${i})">
                ${i}
            </button>
        `;
    }
    
    // Always show last page
    if (currentPage < totalPages - 2) {
        if (currentPage < totalPages - 3) {
            paginationHTML += '<span class="pagination-ellipsis">...</span>';
        }
        paginationHTML += `<button class="pagination-num" onclick="goToPage(${totalPages})">${totalPages}</button>`;
    }
    
    paginationHTML += '</div>';
    
    // Next button
    paginationHTML += `
        <button class="pagination-btn ${currentPage === totalPages ? 'disabled' : ''}" 
                onclick="goToPage(${currentPage + 1})" 
                ${currentPage === totalPages ? 'disabled' : ''}>
            Next <i class="fas fa-chevron-right"></i>
        </button>
    `;
    
    paginationHTML += '</div>';
    paginationElement.innerHTML = paginationHTML;
}

function goToPage(page) {
    const totalPages = Math.ceil(
        auctionsData.items.filter(item => {
            // Apply current filters to calculate total pages
            let matches = true;
            if (auctionsData.searchTerm) {
                matches = item.name.toLowerCase().includes(auctionsData.searchTerm) ||
                         (item.description && item.description.toLowerCase().includes(auctionsData.searchTerm));
            }
            if (matches && auctionsData.filter === 'active') {
                matches = item.active !== false;
            } else if (matches && auctionsData.filter === 'inactive') {
                matches = item.active === false;
            }
            return matches;
        }).length / auctionsData.itemsPerPage
    );
    
    if (page < 1 || page > totalPages) return;
    
    auctionsData.currentPage = page;
    displayAuctions();
    
    // Scroll to top of auctions section
    document.getElementById('adminAuctionsView').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function loadAdminActivity() {
    const container = document.getElementById('adminLiveFeed');
    if (!container) return;
    
    // Show loading state
    container.innerHTML = '<div class="admin-loading"><i class="fas fa-spinner fa-spin"></i> Loading activity...</div>';
    
    // Fetch recent bids
    Promise.all([
        fetch('/api/bids').then(r => r.json()),
        fetch('/api/items').then(r => r.json())
    ])
    .then(([bids, items]) => {
        if (!bids || bids.length === 0) {
            container.innerHTML = `
                <div class="admin-live-feed-empty">
                    <i class="fas fa-inbox"></i>
                    <h3>No Activity Yet</h3>
                    <p>Bid activity will appear here in real-time</p>
                </div>
            `;
            updateActivityStats([], {});
            return;
        }
        
        // Create item lookup map
        const itemMap = {};
        items.forEach(item => {
            itemMap[item.id] = item.name;
        });
        
        // Calculate statistics
        const uniqueBidders = new Set(bids.map(b => b.bidderName)).size;
        const itemBidCounts = {};
        bids.forEach(bid => {
            itemBidCounts[bid.itemId] = (itemBidCounts[bid.itemId] || 0) + 1;
        });
        
        // Find most active item
        let mostActiveItemId = null;
        let maxBids = 0;
        for (const [itemId, count] of Object.entries(itemBidCounts)) {
            if (count > maxBids) {
                maxBids = count;
                mostActiveItemId = itemId;
            }
        }
        const mostActiveItem = mostActiveItemId ? (itemMap[mostActiveItemId] || `Item #${mostActiveItemId}`) : '-';
        
        // Update stats
        updateActivityStats(bids, { uniqueBidders, mostActiveItem });
        
        // Setup activity limit listener
        const limitSelect = document.getElementById('activityLimit');
        if (limitSelect && !limitSelect.dataset.initialized) {
            limitSelect.dataset.initialized = 'true';
            limitSelect.addEventListener('change', () => displayActivityBids(bids, itemMap));
        }
        
        // Display bids
        displayActivityBids(bids, itemMap);
    })
    .catch(error => {
        console.error('Error loading activity:', error);
        container.innerHTML = `
            <div class="admin-error-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Failed to Load Activity</h3>
                <p>${error.message}</p>
                <button class="btn btn-primary" onclick="loadAdminActivity()">
                    <i class="fas fa-sync-alt"></i> Retry
                </button>
            </div>
        `;
    });
}

function displayActivityBids(bids, itemMap) {
    const container = document.getElementById('adminLiveFeed');
    const limitSelect = document.getElementById('activityLimit');
    if (!container) return;
    
    // Get limit
    const limit = limitSelect ? limitSelect.value : '50';
    let displayBids = bids;
    
    if (limit !== 'all') {
        const limitNum = parseInt(limit);
        displayBids = bids.slice(-limitNum);
    }
    
    // Reverse to show newest first
    displayBids = displayBids.reverse();
    
    // Render bid items
    container.innerHTML = displayBids.map(bid => {
        const itemName = itemMap[bid.itemId] || `Item #${bid.itemId}`;
        return `
            <div class="bid-item">
                <div class="bid-item-header">
                    <span class="bid-item-name">${itemName}</span>
                    <span class="bid-item-time">
                        <i class="fas fa-clock"></i>
                        ${getRelativeTime(bid.timestamp)}
                    </span>
                </div>
                <div class="bid-item-details">
                    <span class="bid-item-amount">$${bid.amount.toFixed(2)}</span>
                    <span class="bid-item-bidder">
                        <i class="fas fa-user"></i>
                        ${bid.bidderName}
                    </span>
                </div>
            </div>
        `;
    }).join('');
}

function updateActivityStats(bids, stats) {
    // Update total bids
    const totalBidsEl = document.getElementById('totalActivityBids');
    if (totalBidsEl) {
        totalBidsEl.textContent = bids.length;
    }
    
    // Update unique bidders
    const uniqueBiddersEl = document.getElementById('uniqueBidders');
    if (uniqueBiddersEl) {
        uniqueBiddersEl.textContent = stats.uniqueBidders || 0;
    }
    
    // Update most active item
    const mostActiveItemEl = document.getElementById('mostActiveItem');
    if (mostActiveItemEl) {
        mostActiveItemEl.textContent = stats.mostActiveItem || '-';
    }
}

// Export functions for global access
window.placeBid = placeBid;
window.loadBids = loadBids;
window.showModal = showModal;
window.closeModal = closeModal;
window.showAdminSection = showAdminSection;
window.refreshUsers = refreshUsers;
window.refreshAdminStats = refreshAdminStats;
window.editUserRole = editUserRole;
window.toggleUserStatus = toggleUserStatus;
window.goToPage = goToPage;
window.loadAdminActivity = loadAdminActivity;
