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
        // ADMIN: Show stats and user management view
        placeBidSection.style.display = 'none';
        liveBidsSection.style.display = 'block';
        statsSection.style.display = 'grid';
        auctionsSection.style.display = 'block';
        
        showToast('Welcome Admin! System monitoring and user management access.', 'info');
        
        loadAllRecentBids();
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

// Export functions for global access
window.placeBid = placeBid;
window.loadBids = loadBids;
window.showModal = showModal;
window.closeModal = closeModal;
