/* Modern Bidding Platform with Authentication */
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
    document.getElementById('refreshAuctions').addEventListener('click', loadAuctions);
    
    // Modal close on outside click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });
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
    
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('userSection').style.display = 'flex';
    document.getElementById('userName').textContent = user.firstName + ' ' + user.lastName;
    document.getElementById('userRole').textContent = user.role;
    
    // Enable bidding functionality
    document.getElementById('placeBidBtn').disabled = false;
    document.getElementById('bidAmount').disabled = false;
    document.getElementById('itemSelect').disabled = false;
    
    // Load bids after authentication
    loadAllRecentBids();
}

function setUnauthenticatedUser() {
    currentUser = null;
    authToken = null;
    
    document.getElementById('authSection').style.display = 'flex';
    document.getElementById('userSection').style.display = 'none';
    
    // Disable bidding functionality
    document.getElementById('placeBidBtn').disabled = true;
    document.getElementById('bidAmount').disabled = true;
    document.getElementById('itemSelect').disabled = true;
    
    // Clear bids display for unauthenticated users
    const container = document.getElementById('bids');
    container.innerHTML = '<div class="text-center text-muted">Please login to view live bids</div>';
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
            showToast('Login successful!', 'success');
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
        lastName: document.getElementById('registerLastName').value
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
            showToast('Registration successful!', 'success');
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
    showToast('Logged out successfully', 'info');
}

// WebSocket Functions
function setStatus(connected) {
    const el = document.getElementById('status');
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

        // Subscribe to general auction updates for shared live bids feed
        stompClient.subscribe('/topic/auctions', function (message) {
            const payload = JSON.parse(message.body);
            console.log('Received auction update:', payload);
            if (payload.type === 'BID_UPDATE' || payload.type === 'NEW_BID') {
                addBidToSharedFeed(payload.data);
                updateAuctionDisplay(payload.data);
                updateStats();
            } else if (payload.type === 'AUCTION_UPDATE') {
                updateAuctionDisplay(payload.data);
            }
        });

        loadAuctions();
        updateStats();
        
        // Only load bids if user is authenticated
        if (currentUser) {
            loadAllRecentBids();
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
    select.innerHTML = '<option value="">Choose an item...</option>';
    auctions.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = a.name;
        select.appendChild(opt);
    });
}

async function placeBid() {
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
    container.innerHTML = '';
    bids.forEach(bid => addBidToHtml(container, bid, false));
}

async function loadAllRecentBids() {
    if (!currentUser) {
        return; // Don't load bids if not authenticated
    }
    
    try {
        const response = await fetch('/api/bids');
        const bids = await response.json();
        const container = document.getElementById('bids');
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
        
        document.getElementById('totalBids').textContent = bids.length;
        document.getElementById('activeAuctions').textContent = items.length;
        
        const highestBid = Math.max(...bids.map(b => b.amount), 0);
        document.getElementById('highestBid').textContent = `$${highestBid.toFixed(2)}`;
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

// Utility Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

function formatTime(date) {
    return new Date(date).toLocaleString();
}

// Export functions for global access
window.placeBid = placeBid;
window.loadBids = loadBids;
window.showModal = showModal;
window.closeModal = closeModal;