document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const userInfo = document.getElementById('user-info');
    const userName = document.getElementById('user-name');
    const userType = document.getElementById('user-type');
    const logoutBtn = document.getElementById('logout-btn');
    const notification = document.getElementById('notification');
    const notificationMessage = document.getElementById('notification-message');
    const loadingOverlay = document.getElementById('loading-overlay');

    // Global state
    let currentUser = null;
    let currentUserType = null;

    // Utility Functions
    function showScreen(screenId) {
        document.querySelectorAll('.screen').forEach(screen => screen.classList.remove('active'));
        const targetScreen = document.getElementById(screenId);
        if (targetScreen) {
            targetScreen.classList.add('active');
        }
    }

    function showNotification(message, type = 'success') {
        notificationMessage.textContent = message;
        notification.classList.remove('error', 'success');
        notification.classList.add(type);
        notification.classList.add('show');
        
        setTimeout(() => {
            notification.classList.remove('show');
        }, 4000);
    }

    function showLoading() {
        loadingOverlay.classList.add('show');
    }

    function hideLoading() {
        loadingOverlay.classList.remove('show');
    }

    function updateUserInfo(user) {
        if (user) {
            currentUser = user;
            currentUserType = user.userType;
            userName.textContent = user.name;
            userType.textContent = user.userType.charAt(0).toUpperCase() + user.userType.slice(1);
            userInfo.style.display = 'flex';
            
            // Initialize points for new user session
            if (currentUserType === 'student') {
                currentPoints = 50; // Give students 50 starting points
                console.log('Initialized points for student:', currentPoints);
                
                // Update points displays immediately
                setTimeout(() => {
                    updateAllPointsDisplays();
                }, 100);
            }
        } else {
            currentUser = null;
            currentUserType = null;
            userInfo.style.display = 'none';
        }
    }

    // Simulated API Functions
    function apiCall(endpoint, method = 'GET', data = null) {
        return new Promise((resolve) => {
            setTimeout(() => {
                if (endpoint === '/api/login') {
                    const users = {
                        'student@example.com': { name: 'Student User', userType: 'student' },
                        'staff@cafeteria.com': { name: 'Staff User', userType: 'staff' },
                        'test@test.com': { name: 'Test User', userType: 'student' },
                        'admin@admin.com': { name: 'Admin User', userType: 'staff' }
                    };
                    const user = users[data.email];
                    if (user && (data.password === 'password123' || data.password === 'test' || data.password === 'admin')) {
                        resolve({ success: true, name: user.name, email: data.email, userType: user.userType });
                    } else {
                        resolve({ success: false, message: 'Invalid credentials. Try: student@example.com / password123 or test@test.com / test' });
                    }
                } else if (endpoint === '/api/register') {
                    resolve({ success: true, message: 'Registration successful' });
                } else if (endpoint === '/api/logout') {
                    resolve({ success: true, message: 'Logged out successfully' });
                } else if (endpoint === '/api/loyaltyPoints') {
                    resolve({ success: true, points: 50 }); // Give more starting points
                } else {
                    resolve({ success: false, message: 'Invalid endpoint' });
                }
            }, 500); // Reduce delay for better UX
        });
    }

    // Navigation Functions
    window.showLogin = (type) => {
        document.getElementById('login-title').textContent = `${type.charAt(0).toUpperCase() + type.slice(1)} Login`;
        showScreen('login-screen');
    };

    window.showRegister = () => showScreen('register-screen');
    window.showWelcome = () => showScreen('welcome-screen');

    window.showStudentDashboard = () => {
        if (currentUserType === 'student') {
            showScreen('student-dashboard');
            loadStudentData();
        } else {
            showNotification('Access denied. Please login as a student.', 'error');
        }
    };

    window.showStaffDashboard = () => {
        if (currentUserType === 'staff') {
            showScreen('staff-dashboard');
        } else {
            showNotification('Access denied. Please login as staff.', 'error');
        }
    };

    // Global variables for cart and menu
    let cart = [];
    let menuItems = [];
    let currentPoints = 50; // Start with 50 points for testing
    let pendingOrders = []; // Store pending orders with status

    // Sample menu data
    const sampleMenu = [
        { id: 1, name: "Classic Burger", price: 8.99, description: "Juicy beef patty with lettuce, tomato, and our special sauce" },
        { id: 2, name: "Margherita Pizza", price: 12.99, description: "Fresh mozzarella, tomato sauce, and basil on thin crust" },
        { id: 3, name: "Caesar Salad", price: 7.99, description: "Crisp romaine lettuce with parmesan and croutons" },
        { id: 4, name: "Chicken Wrap", price: 6.99, description: "Grilled chicken with vegetables in a soft tortilla" },
        { id: 5, name: "French Fries", price: 3.99, description: "Golden crispy fries with sea salt" },
        { id: 6, name: "Coca Cola", price: 2.49, description: "Refreshing cola drink" },
        { id: 7, name: "Coffee", price: 2.99, description: "Freshly brewed coffee" },
        { id: 8, name: "Chocolate Cake", price: 4.99, description: "Rich chocolate cake with frosting" }
    ];

    // Menu functionality
    window.showMenu = async () => {
        if (currentUserType === 'student') {
            showScreen('menu-screen');
            await loadMenu();
        } else {
            showNotification('Access denied. Please login as a student.', 'error');
        }
    };

    // Order functionality
    window.showOrderForm = async () => {
        if (currentUserType === 'student') {
            showScreen('order-screen');
            await loadOrderMenu();
            updateCartDisplay();
        } else {
            showNotification('Access denied. Please login as a student.', 'error');
        }
    };

    // Loyalty points functionality
    window.showLoyaltyPoints = async () => {
        if (currentUserType === 'student') {
            showScreen('loyalty-screen');
            await loadLoyaltyPoints();
        } else {
            showNotification('Access denied. Please login as a student.', 'error');
        }
    };

    // Redeem points functionality
    window.showRedeemPoints = async () => {
        if (currentUserType === 'student') {
            console.log('Showing redeem points screen, current points:', currentPoints);
            showScreen('redeem-screen');
            await loadRedeemMenu();
            
            // Wait a bit for the DOM to be ready, then update the redeem value
            setTimeout(() => {
                setupRedeemEventListeners();
                window.updateRedeemValue();
                console.log('Redeem screen initialized');
            }, 200);
        } else {
            showNotification('Access denied. Please login as a student.', 'error');
        }
    };

    // Staff functionality
    window.showPendingOrders = async () => {
        if (currentUserType === 'staff') {
            showScreen('pending-orders-screen');
            await loadPendingOrders();
            
            // Set up auto-refresh for pending orders (every 5 seconds)
            if (window.pendingOrdersInterval) {
                clearInterval(window.pendingOrdersInterval);
            }
            window.pendingOrdersInterval = setInterval(async () => {
                if (document.getElementById('pending-orders-screen').style.display !== 'none') {
                    await loadPendingOrders();
                }
            }, 5000);
        } else {
            showNotification('Access denied. Please login as staff.', 'error');
        }
    };

    window.showUpdateOrderStatus = () => {
        if (currentUserType === 'staff') {
            showScreen('update-order-screen');
        } else {
            showNotification('Access denied. Please login as staff.', 'error');
        }
    };

    window.showSalesReport = () => {
        if (currentUserType === 'staff') {
            showScreen('sales-report-screen');
        } else {
            showNotification('Access denied. Please login as staff.', 'error');
        }
    };

    // Helper function to update all points displays
    function updateAllPointsDisplays() {
        console.log('Updating all points displays, current points:', currentPoints);
        
        // Update student dashboard points
        const studentPointsElement = document.getElementById('student-points');
        if (studentPointsElement) {
            studentPointsElement.textContent = currentPoints;
            console.log('Updated student dashboard points to:', currentPoints);
        }
        
        // Update loyalty screen points
        const currentPointsElement = document.getElementById('current-points');
        if (currentPointsElement) {
            currentPointsElement.textContent = currentPoints;
            console.log('Updated loyalty screen points to:', currentPoints);
        }
    }

    // Load student data
    async function loadStudentData() {
        if (currentUser && currentUserType === 'student') {
            try {
                showLoading();
                console.log('Loading student data, current points:', currentPoints);
                
                // Update all points displays
                updateAllPointsDisplays();
                
                // Simulate loading delay
                await new Promise(resolve => setTimeout(resolve, 500));
                
            } catch (error) {
                console.error('Failed to load student data:', error);
            } finally {
                hideLoading();
            }
        }
    }

    // Login Handler
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const loginTitle = document.getElementById('login-title').textContent.toLowerCase();

        // Check specific fields
        if (!email) {
            showNotification('Please enter an email address.', 'error');
            return;
        }
        if (!password) {
            showNotification('Please enter a password.', 'error');
            return;
        }

        const userType = loginTitle.includes('student') ? 'student' : 'staff';

        try {
            showLoading();
            
            const response = await apiCall('/api/login', 'POST', {
                email: email,
                password: password,
                userType: userType
            });

            if (response.success) {
                updateUserInfo({
                    name: response.name,
                    email: response.email,
                    userType: response.userType
                });

                showNotification(`Welcome back, ${response.name}!`, 'success');
                
                // Navigate to appropriate dashboard
                if (response.userType === 'student') {
                    showStudentDashboard();
                } else {
                    showStaffDashboard();
                }
            } else {
                showNotification(response.message || 'Login failed. Please check your credentials.', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            showNotification('Login failed. Please try again later.', 'error');
        } finally {
            hideLoading();
        }
    });

    // Register Handler
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const userType = document.getElementById('user-type').value;

        if (!name || !email || !password || !userType) {
            showNotification('Please fill in all fields.', 'error');
            return;
        }

        // Basic email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showNotification('Please enter a valid email address.', 'error');
            return;
        }

        // Password validation
        if (password.length < 6) {
            showNotification('Password must be at least 6 characters long.', 'error');
            return;
        }

        // Staff email validation
        if (userType === 'staff' && !email.endsWith('@cafeteria.com')) {
            showNotification('Staff email must end with @cafeteria.com', 'error');
            return;
        }

        // Student email validation
        if (userType === 'student' && email.endsWith('@cafeteria.com')) {
            showNotification('Student email cannot end with @cafeteria.com', 'error');
            return;
        }

        try {
            showLoading();
            
            const response = await apiCall('/api/register', 'POST', {
                name: name,
                email: email,
                password: password,
                userType: userType
            });

            if (response.success) {
                showNotification('Account created successfully! Please login.', 'success');
                showLogin(userType);
                
                // Clear form
                registerForm.reset();
            } else {
                showNotification(response.message || 'Registration failed. Please try again.', 'error');
            }
        } catch (error) {
            console.error('Registration error:', error);
            showNotification('Registration failed. Please try again later.', 'error');
        } finally {
            hideLoading();
        }
    });

    // Logout Handler
    logoutBtn.addEventListener('click', async () => {
        try {
            await apiCall('/api/logout', 'POST');
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            updateUserInfo(null);
            showScreen('welcome-screen');
            showNotification('Logged out successfully.', 'success');
        }
    });

    // Initialize app
    function init() {
        const savedUser = localStorage.getItem('cafeteriaUser');
        if (savedUser) {
            try {
                const user = JSON.parse(savedUser);
                updateUserInfo(user);
                if (user.userType === 'student') {
                    showStudentDashboard();
                } else {
                    showStaffDashboard();
                }
            } catch (error) {
                console.error('Failed to restore user session:', error);
                localStorage.removeItem('cafeteriaUser');
            }
        }

        const originalUpdateUserInfo = updateUserInfo;
        updateUserInfo = (user) => {
            originalUpdateUserInfo(user);
            if (user) {
                localStorage.setItem('cafeteriaUser', JSON.stringify(user));
            } else {
                localStorage.removeItem('cafeteriaUser');
            }
        };
    }

    // Start the application
    init();

    // Add some professional touches
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const activeScreen = document.querySelector('.screen.active');
            if (activeScreen && activeScreen.id !== 'welcome-screen') {
                if (currentUser) {
                    if (currentUserType === 'student') {
                        showStudentDashboard();
                    } else {
                        showStaffDashboard();
                    }
                } else {
                    showWelcome();
                }
            }
        }
    });

    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('mouseenter', () => {
            btn.style.transform = 'translateY(-2px)';
        });
        
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'translateY(0)';
        });
    });

    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', () => {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
                
                setTimeout(() => {
                    submitBtn.disabled = false;
                    if (form.id === 'login-form') {
                        submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
                    } else if (form.id === 'register-form') {
                        submitBtn.innerHTML = '<i class="fas fa-user-plus"></i> Create Account';
                    }
                }, 2000);
            }
        });
    });

    // Menu loading functions
    async function loadMenu() {
        try {
            showLoading();
            // Use sample data for now
            menuItems = sampleMenu;
            displayMenuItems('menu-items');
        } catch (error) {
            console.error('Failed to load menu:', error);
            showNotification('Failed to load menu. Please try again.', 'error');
        } finally {
            hideLoading();
        }
    }

    async function loadOrderMenu() {
        try {
            showLoading();
            menuItems = [...sampleMenu]; // Create a copy
            console.log('Loaded order menu items:', menuItems);
            displayOrderMenuItems('order-menu-items');
        } catch (error) {
            console.error('Failed to load order menu:', error);
            showNotification('Failed to load menu. Please try again.', 'error');
        } finally {
            hideLoading();
        }
    }

    async function loadRedeemMenu() {
        try {
            showLoading();
            menuItems = [...sampleMenu]; // Create a copy
            console.log('Loaded redeem menu items:', menuItems);
            console.log('Current points for redeem menu:', currentPoints);
            
            // Wait a bit for DOM to be ready
            await new Promise(resolve => setTimeout(resolve, 100));
            
            displayOrderMenuItems('redeem-menu-items');
            console.log('Redeem menu displayed');
        } catch (error) {
            console.error('Failed to load redeem menu:', error);
            showNotification('Failed to load menu. Please try again.', 'error');
        } finally {
            hideLoading();
        }
    }

    function displayMenuItems(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.innerHTML = menuItems.map(item => `
            <div class="menu-item">
                <div class="menu-item-header">
                    <div class="menu-item-name">${item.name}</div>
                    <div class="menu-item-price">$${item.price.toFixed(2)}</div>
                </div>
                <div class="menu-item-description">${item.description}</div>
            </div>
        `).join('');
    }

    function displayOrderMenuItems(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // Check if this is the redeem menu
        const isRedeemMenu = containerId === 'redeem-menu-items';
        
        container.innerHTML = menuItems.map(item => {
            const pointsNeeded = Math.ceil(item.price / 0.2); // 1 point = $0.20
            
            if (isRedeemMenu) {
                // For redeem menu, show points needed and redeem button
                return `
                    <div class="menu-item">
                        <div class="menu-item-header">
                            <div class="menu-item-name">${item.name}</div>
                            <div class="menu-item-price">${pointsNeeded} points</div>
                        </div>
                        <div class="menu-item-description">${item.description}</div>
                        <div class="menu-item-actions">
                            <button class="btn btn-primary" onclick="redeemMenuItem(${item.id}, ${pointsNeeded})" 
                                    ${currentPoints < pointsNeeded ? 'disabled' : ''}>
                                <i class="fas fa-gift"></i> Redeem for ${pointsNeeded} points
                            </button>
                            ${currentPoints < pointsNeeded ? '<small style="color: #dc3545;">Not enough points</small>' : ''}
                        </div>
                    </div>
                `;
            } else {
                // For regular order menu, show price and add to cart
                return `
                    <div class="menu-item">
                        <div class="menu-item-header">
                            <div class="menu-item-name">${item.name}</div>
                            <div class="menu-item-price">$${item.price.toFixed(2)}</div>
                        </div>
                        <div class="menu-item-description">${item.description}</div>
                        <div class="menu-item-actions">
                            <div class="quantity-controls">
                                <button class="quantity-btn" onclick="decreaseQuantity(${item.id})">-</button>
                                <span class="quantity-display" id="qty-${item.id}">0</span>
                                <button class="quantity-btn" onclick="increaseQuantity(${item.id})">+</button>
                            </div>
                            <button class="btn btn-primary" onclick="addToCart(${item.id})">
                                <i class="fas fa-plus"></i> Add
                            </button>
                        </div>
                    </div>
                `;
            }
        }).join('');
    }

    // Cart functionality - Make these global functions
    window.increaseQuantity = function(itemId) {
        const qtyElement = document.getElementById(`qty-${itemId}`);
        if (qtyElement) {
            const currentQty = parseInt(qtyElement.textContent) || 0;
            qtyElement.textContent = currentQty + 1;
        }
    };

    window.decreaseQuantity = function(itemId) {
        const qtyElement = document.getElementById(`qty-${itemId}`);
        if (qtyElement) {
            const currentQty = parseInt(qtyElement.textContent) || 0;
            if (currentQty > 0) {
                qtyElement.textContent = currentQty - 1;
            }
        }
    };

    window.addToCart = function(itemId) {
        console.log('addToCart called with itemId:', itemId);
        console.log('Current menuItems:', menuItems);
        console.log('Current cart:', cart);
        
        const qtyElement = document.getElementById(`qty-${itemId}`);
        if (!qtyElement) {
            console.error('Quantity element not found for itemId:', itemId);
            showNotification('Quantity element not found.', 'error');
            return;
        }
        
        const quantity = parseInt(qtyElement.textContent) || 0;
        console.log('Quantity selected:', quantity);
        
        if (quantity <= 0) {
            showNotification('Please select a quantity first.', 'error');
            return;
        }

        const item = menuItems.find(i => i.id === itemId);
        if (!item) {
            console.error('Item not found in menuItems for itemId:', itemId);
            showNotification('Item not found.', 'error');
            return;
        }

        console.log('Found item:', item);

        const existingItem = cart.find(c => c.id === itemId);
        if (existingItem) {
            existingItem.quantity += quantity;
            console.log('Updated existing item in cart');
        } else {
            cart.push({
                id: item.id,
                name: item.name,
                price: item.price,
                quantity: quantity
            });
            console.log('Added new item to cart');
        }

        qtyElement.textContent = '0';
        updateCartDisplay();
        showNotification(`${quantity} x ${item.name} added to cart!`, 'success');
    };

    function updateCartDisplay() {
        const cartItemsContainer = document.getElementById('cart-items');
        const placeOrderBtn = document.getElementById('place-order-btn');
        
        console.log('Updating cart display, cart length:', cart.length);
        
        if (!cartItemsContainer) {
            console.error('Cart items container not found');
            return;
        }
        
        if (cart.length === 0) {
            cartItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
            if (placeOrderBtn) placeOrderBtn.disabled = true;
        } else {
            cartItemsContainer.innerHTML = cart.map(item => `
                <div class="cart-item">
                    <div class="cart-item-name">${item.name}</div>
                    <div class="cart-item-quantity">x${item.quantity}</div>
                    <div class="cart-item-price">$${(item.price * item.quantity).toFixed(2)}</div>
                </div>
            `).join('');
            if (placeOrderBtn) placeOrderBtn.disabled = false;
        }

        updateCartTotals();
    }

    function updateCartTotals() {
        const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const tax = subtotal * 0.10; // 10% tax
        const total = subtotal + tax;
        const pointsEarned = Math.floor(total / 10); // 1 point per $10

        document.getElementById('cart-subtotal').textContent = `$${subtotal.toFixed(2)}`;
        document.getElementById('cart-tax').textContent = `$${tax.toFixed(2)}`;
        document.getElementById('cart-total').textContent = `$${total.toFixed(2)}`;
        document.getElementById('points-earned').textContent = pointsEarned;
    }

    // Place order functionality
    window.placeOrder = async () => {
        if (cart.length === 0) {
            showNotification('Your cart is empty!', 'error');
            return;
        }

        try {
            showLoading();
            
            // Simulate order placement
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            const tax = subtotal * 0.10;
            const total = subtotal + tax;
            const pointsEarned = Math.floor(total / 10);
            
            // Generate new order ID
            const newOrderId = 2000 + pendingOrders.length + 1;
            
            // Create order details string
            const orderItems = cart.map(item => `${item.name} x${item.quantity}`).join(', ');
            
            // Create new order object
            const newOrder = {
                id: newOrderId,
                student: currentUser ? currentUser.name : 'Student User',
                total: total,
                date: new Date().toLocaleString(),
                status: 'pending',
                items: orderItems,
                subtotal: subtotal,
                tax: tax,
                pointsEarned: pointsEarned
            };
            
            // Add to pending orders
            pendingOrders.push(newOrder);
            
            console.log('New order created:', newOrder);
            console.log('Total pending orders:', pendingOrders.length);
            
            // Notify staff about new order (if they're viewing pending orders)
            if (currentUserType === 'staff') {
                showNotification(`New order #${newOrderId} received!`, 'success');
            }
            
            // Add points to user
            currentPoints += pointsEarned;
            
            showNotification(`Order #${newOrderId} placed successfully! You earned ${pointsEarned} points.`, 'success');
            
            // Clear cart
            cart = [];
            updateCartDisplay();
            
            // Update all points displays
            updateAllPointsDisplays();
            
            // Go back to dashboard
            setTimeout(() => {
                showStudentDashboard();
            }, 2000);
            
        } catch (error) {
            console.error('Failed to place order:', error);
            showNotification('Failed to place order. Please try again.', 'error');
        } finally {
            hideLoading();
        }
    };

    // Loyalty points functionality
    async function loadLoyaltyPoints() {
        try {
            showLoading();
            console.log('Loading loyalty points, current points:', currentPoints);
            
            // Simulate loading points from API
            await new Promise(resolve => setTimeout(resolve, 500));
            
            // Update all points displays
            updateAllPointsDisplays();
        } catch (error) {
            console.error('Failed to load loyalty points:', error);
            showNotification('Failed to load loyalty points.', 'error');
        } finally {
            hideLoading();
        }
    }

    window.loadLoyaltyHistory = () => {
        showNotification('Loyalty history feature coming soon!', 'success');
    };

    // Redeem points functionality - Make this global
    window.updateRedeemValue = function() {
        const pointsInput = document.getElementById('points-to-redeem');
        const valueDisplay = document.getElementById('redeem-value');
        
        console.log('updateRedeemValue called');
        
        if (pointsInput && valueDisplay) {
            const points = parseInt(pointsInput.value) || 0;
            const value = points * 0.2; // 1 point = $0.20
            valueDisplay.textContent = value.toFixed(2);
            console.log(`Updated redeem value: ${points} points = $${value.toFixed(2)}`);
        } else {
            console.error('Points input or value display not found');
        }
    };

    window.startRedeemProcess = () => {
        console.log('startRedeemProcess called');
        console.log('Current points:', currentPoints);
        
        const pointsInput = document.getElementById('points-to-redeem');
        if (!pointsInput) {
            console.error('Points input not found');
            showNotification('Points input not found.', 'error');
            return;
        }
        
        const points = parseInt(pointsInput.value) || 0;
        console.log('Points to redeem:', points);
        
        if (points < 10) {
            showNotification('Minimum redemption is 10 points.', 'error');
            return;
        }
        
        if (points > currentPoints) {
            showNotification(`Not enough points! You have ${currentPoints} points, trying to redeem ${points}.`, 'error');
            return;
        }
        
        // Simulate redemption process
        const value = points * 0.2;
        currentPoints -= points;
        
        showNotification(`Successfully redeemed ${points} points for $${value.toFixed(2)}! New balance: ${currentPoints} points`, 'success');
        
        // Update all points displays
        updateAllPointsDisplays();
        
        // Reset the input
        pointsInput.value = '10';
        updateRedeemValue();
    };

    // Redeem specific menu item
    window.redeemMenuItem = function(itemId, pointsNeeded) {
        console.log(`redeemMenuItem called for item ${itemId}, points needed: ${pointsNeeded}`);
        console.log(`Current points before redemption: ${currentPoints}`);
        
        if (currentPoints < pointsNeeded) {
            showNotification(`Not enough points! You need ${pointsNeeded} points but only have ${currentPoints}.`, 'error');
            return;
        }
        
        const item = menuItems.find(i => i.id === itemId);
        if (!item) {
            showNotification('Item not found.', 'error');
            return;
        }
        
        // Deduct points
        currentPoints -= pointsNeeded;
        console.log(`Points after redemption: ${currentPoints}`);
        
        showNotification(`Successfully redeemed ${item.name} for ${pointsNeeded} points! New balance: ${currentPoints} points`, 'success');
        
        // Update all points displays
        updateAllPointsDisplays();
        
        // Refresh the redeem menu to update button states
        setTimeout(() => {
            displayOrderMenuItems('redeem-menu-items');
            console.log('Redeem menu refreshed after redemption');
        }, 100);
    };

    // Staff functionality
    async function loadPendingOrders() {
        try {
            showLoading();
            console.log('Loading pending orders...');
            
            // Initialize orders if empty
            if (pendingOrders.length === 0) {
                pendingOrders = [
                    { id: 1001, student: "John Doe", total: 15.99, date: "2024-01-15 14:30", status: "pending" },
                    { id: 1002, student: "Jane Smith", total: 8.99, date: "2024-01-15 14:45", status: "pending" },
                    { id: 1003, student: "Bob Johnson", total: 22.97, date: "2024-01-15 15:00", status: "pending" }
                ];
                console.log('Initialized pending orders:', pendingOrders);
            }
            
            // Simulate loading delay
            await new Promise(resolve => setTimeout(resolve, 500));
            
            const ordersList = document.getElementById('pending-orders-list');
            if (!ordersList) {
                console.error('Orders list container not found');
                return;
            }
            
            ordersList.innerHTML = pendingOrders.map(order => {
                const statusClass = `status-${order.status}`;
                const statusText = order.status.charAt(0).toUpperCase() + order.status.slice(1);
                
                return `
                    <div class="order-item">
                        <div class="order-info">
                            <div class="order-id">Order #${order.id}</div>
                            <div class="order-details">${order.student} • $${order.total.toFixed(2)} • ${order.date}</div>
                            ${order.items ? `<div class="order-items" style="font-size: 0.8rem; color: #ccc; margin-top: 0.3rem;">${order.items}</div>` : ''}
                        </div>
                        <div class="order-status ${statusClass}">${statusText}</div>
                        <div class="order-actions">
                            <select onchange="quickUpdateStatus(${order.id}, this.value)" style="padding: 0.3rem; border-radius: 4px; border: 1px solid #ddd; font-size: 0.8rem;">
                                <option value="">Quick Update</option>
                                <option value="processing">Processing</option>
                                <option value="preparing">Preparing</option>
                                <option value="ready">Ready</option>
                                <option value="completed">Completed</option>
                                <option value="cancelled">Cancelled</option>
                            </select>
                        </div>
                    </div>
                `;
            }).join('');
            
            console.log('Displayed pending orders with current statuses');
            
        } catch (error) {
            console.error('Failed to load pending orders:', error);
            showNotification('Failed to load pending orders.', 'error');
        } finally {
            hideLoading();
        }
    }

    window.updateOrderStatus = async () => {
        const orderId = document.getElementById('order-id').value;
        const newStatus = document.getElementById('new-status').value;
        
        console.log(`Updating order ${orderId} to status: ${newStatus}`);
        
        if (!orderId || !newStatus) {
            showNotification('Please fill in all fields.', 'error');
            return;
        }
        
        try {
            showLoading();
            
            // Find the order in our pending orders array
            const orderIndex = pendingOrders.findIndex(order => order.id == orderId);
            
            if (orderIndex === -1) {
                showNotification(`Order #${orderId} not found.`, 'error');
                return;
            }
            
            // Update the order status
            const oldStatus = pendingOrders[orderIndex].status;
            pendingOrders[orderIndex].status = newStatus.toLowerCase();
            
            console.log(`Order ${orderId} status changed from ${oldStatus} to ${newStatus}`);
            
            // Simulate update delay
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            showNotification(`Order #${orderId} status updated from ${oldStatus} to ${newStatus}!`, 'success');
            
            // Clear form
            document.getElementById('order-id').value = '';
            document.getElementById('new-status').value = '';
            
            // Refresh the pending orders display to show the updated status
            await loadPendingOrders();
            
        } catch (error) {
            console.error('Failed to update order status:', error);
            showNotification('Failed to update order status.', 'error');
        } finally {
            hideLoading();
        }
    };

    // Quick status update function for dropdown
    window.quickUpdateStatus = async (orderId, newStatus) => {
        if (!newStatus) return; // Don't do anything if no status selected
        
        console.log(`Quick updating order ${orderId} to status: ${newStatus}`);
        
        try {
            // Find the order in our pending orders array
            const orderIndex = pendingOrders.findIndex(order => order.id == orderId);
            
            if (orderIndex === -1) {
                showNotification(`Order #${orderId} not found.`, 'error');
                return;
            }
            
            // Update the order status
            const oldStatus = pendingOrders[orderIndex].status;
            pendingOrders[orderIndex].status = newStatus.toLowerCase();
            
            console.log(`Order ${orderId} status changed from ${oldStatus} to ${newStatus}`);
            
            showNotification(`Order #${orderId} status updated to ${newStatus}!`, 'success');
            
            // Refresh the pending orders display to show the updated status
            await loadPendingOrders();
            
        } catch (error) {
            console.error('Failed to quick update order status:', error);
            showNotification('Failed to update order status.', 'error');
        }
    };

    window.generateReport = async (type) => {
        try {
            showLoading();
            // Simulate report generation
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            const reportContent = document.getElementById('report-content');
            const sampleData = {
                daily: { orders: 45, revenue: 387.50, avgOrder: 8.61 },
                weekly: { orders: 312, revenue: 2684.75, avgOrder: 8.61 }
            };
            
            const data = sampleData[type];
            
            reportContent.innerHTML = `
                <div class="report-stats">
                    <div class="stat-card">
                        <div class="stat-value">${data.orders}</div>
                        <div class="stat-label">Total Orders</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-value">$${data.revenue.toFixed(2)}</div>
                        <div class="stat-label">Total Revenue</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-value">$${data.avgOrder.toFixed(2)}</div>
                        <div class="stat-label">Average Order</div>
                    </div>
                </div>
                <p>${type.charAt(0).toUpperCase() + type.slice(1)} report generated successfully!</p>
            `;
            
            showNotification(`${type.charAt(0).toUpperCase() + type.slice(1)} report generated!`, 'success');
            
        } catch (error) {
            console.error('Failed to generate report:', error);
            showNotification('Failed to generate report.', 'error');
        } finally {
            hideLoading();
        }
    };

    // Event listeners for redeem points - Set up when redeem screen is shown
    function setupRedeemEventListeners() {
        const pointsInput = document.getElementById('points-to-redeem');
        if (pointsInput) {
            // Remove existing listeners to avoid duplicates
            pointsInput.removeEventListener('input', window.updateRedeemValue);
            pointsInput.addEventListener('input', window.updateRedeemValue);
            console.log('Added event listener to points input');
        } else {
            console.log('Points input not found during setup');
        }
    }

    console.log('🚀 University Cafeteria System initialized successfully!');
});