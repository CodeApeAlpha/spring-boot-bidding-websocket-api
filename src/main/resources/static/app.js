/* Minimal client app to wire REST + WebSocket */
let stompClient = null;
let currentItemId = null;
let currentBidSubscription = null; // holds the active item-specific subscription

function setStatus(connected) {
  const el = document.getElementById('status');
  el.textContent = connected ? 'Connected' : 'Disconnected';
  el.className = 'status ' + (connected ? 'connected' : 'disconnected');
}

function connect() {
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
      } else if (payload.type === 'AUCTION_UPDATE') {
        updateAuctionDisplay(payload.data);
      }
    });

    // Item-specific subscription will be established dynamically in loadBids()

    loadAuctions();
    loadAllRecentBids(); // Load shared live bids feed
  }, function(error) {
    console.error('STOMP error: ' + error);
    setStatus(false);
  });
}

function disconnect() {
  if (stompClient) stompClient.disconnect();
  setStatus(false);
}

function loadAuctions() {
  fetch('/api/items/active')
    .then(r => r.json())
    .then(items => { displayAuctions(items); populateItemSelect(items); })
    .catch(e => console.error('Error loading auctions:', e));
}

function displayAuctions(auctions) {
  const container = document.getElementById('auctions');
  container.innerHTML = '';
  auctions.forEach(auction => {
    const div = document.createElement('div');
    div.className = 'auction-item';
    div.innerHTML = `
      <h3>${auction.name}</h3>
      <p class="meta">${auction.description || ''}</p>
      <p><strong>Current Highest Bid: $${auction.currentHighestBid}</strong></p>
      <p class="meta">Ends: ${new Date(auction.endTime).toLocaleString()}</p>
      <div class="row">
        <button onclick="loadBids(${auction.id})">View Bids</button>
      </div>
    `;
    container.appendChild(div);
  });
}

function populateItemSelect(auctions) {
  const select = document.getElementById('itemSelect');
  select.innerHTML = '<option value="">Select an item...</option>';
  auctions.forEach(a => {
    const opt = document.createElement('option');
    opt.value = a.id; opt.textContent = a.name; select.appendChild(opt);
  });
}

function placeBid() {
  const itemId = document.getElementById('itemSelect').value;
  const bidderName = document.getElementById('bidderName').value;
  const amount = document.getElementById('bidAmount').value;
  if (!itemId || !bidderName || !amount) return alert('Please fill in all fields');

  const dto = { itemId: parseInt(itemId), bidderName, amount: parseFloat(amount) };
  fetch('/api/bids', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) })
    .then(res => { 
      if (res.ok) { 
        document.getElementById('bidAmount').value=''; 
        loadBids(itemId); 
        // WebSocket will handle real-time updates automatically
      } else {
        res.text().then(text => alert('Failed to place bid: ' + text));
      }
    })
    .catch(e => console.error('Error placing bid:', e));
}

function loadBids(itemId) {
  currentItemId = parseInt(itemId);
  // Load bids for specific item (for detailed view)
  fetch(`/api/bids/item/${itemId}`)
    .then(r => r.json())
    .then(displayBids)
    .catch(e => console.error('Error loading bids:', e));
}

function displayBids(bids) {
  const container = document.getElementById('bids');
  container.innerHTML = '';
  bids.forEach(bid => addBidToHtml(container, bid, false));
}

function loadAllRecentBids() {
  // Load recent bids from all items for the shared feed
  fetch('/api/bids')
    .then(r => r.json())
    .then(bids => {
      const container = document.getElementById('bids');
      container.innerHTML = '';
      // Sort by timestamp descending (newest first)
      bids.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      bids.forEach(bid => addBidToHtml(container, bid, false));
    })
    .catch(e => console.error('Error loading all bids:', e));
}

function addBidToHtml(container, bid, prepend = true) {
  const div = document.createElement('div');
  div.className = 'bid-item' + (bid.isWinning ? ' winning-bid' : '');
  div.innerHTML = `
    <strong>$${bid.amount}</strong> by ${bid.bidderName}
    <span style="float:right;">${new Date(bid.timestamp).toLocaleTimeString()}</span>
    <div style="font-size:0.8em;color:#666;">Item ID: ${bid.itemId}</div>
  `;
  if (prepend) container.insertBefore(div, container.firstChild); else container.appendChild(div);
}

function addBidToList(bid) {
  if (!currentItemId || bid.itemId !== currentItemId) return;
  const container = document.getElementById('bids');
  addBidToHtml(container, bid, true);
}

function addBidToSharedFeed(bid) {
  // Add all bids to the shared live bids feed regardless of item
  const container = document.getElementById('bids');
  addBidToHtml(container, bid, true);
}

function updateAuctionDisplay(bid) {
  // Update the auction display with new bid information
  const auctions = document.querySelectorAll('.auction-item');
  auctions.forEach(auction => {
    const auctionId = auction.querySelector('button').onclick.toString().match(/loadBids\((\d+)\)/);
    if (auctionId && parseInt(auctionId[1]) === bid.itemId) {
      const currentBidElement = auction.querySelector('strong');
      if (currentBidElement) {
        currentBidElement.textContent = `Current Highest Bid: $${bid.amount}`;
      }
    }
  });
}

window.onload = connect;

// Subscribe to the item-specific bids topic, cleaning up prior subscription
function subscribeToItemBids(itemId) {
  if (!stompClient || !stompClient.connected) return;
  // Unsubscribe previous
  if (currentBidSubscription && typeof currentBidSubscription.unsubscribe === 'function') {
    try { currentBidSubscription.unsubscribe(); } catch (e) { console.warn('Unsubscribe failed', e); }
  }
  // Subscribe new
  const destination = `/topic/bids/${itemId}`;
  currentBidSubscription = stompClient.subscribe(destination, function (message) {
    const payload = JSON.parse(message.body);
    console.log('Received item bid update:', payload);
    if (payload.type === 'NEW_BID') {
      addBidToList(payload.data);
      updateAuctionDisplay(payload.data);
    } else if (payload.type === 'CURRENT_BIDS') {
      // Optional: hydrate with current bids when (re)subscribing
      const container = document.getElementById('bids');
      container.innerHTML = '';
      (payload.data || []).forEach(b => addBidToHtml(container, b, false));
    }
  });
}


