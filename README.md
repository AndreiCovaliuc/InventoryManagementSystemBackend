# Inventory Management System - Backend

A Spring Boot REST API for multi-tenant inventory management with real-time tracking, analytics, and AI assistant capabilities.

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.3**
- **PostgreSQL**
- **Spring Security + JWT** (JJWT v0.11.5)
- **Spring Data JPA + Hibernate**
- **WebSocket + STOMP** (Real-time updates)
- **Spring AI + Ollama** (AI Assistant)
- **Apache POI** (Excel export)
- **Lombok**
- **Maven**

## Project Structure

```
src/main/java/com/example/inventory_backend/
├── config/                    # Configuration classes
│   ├── ScheduledTasks.java            # Daily automated stats snapshots
│   ├── SimpleCorsFilter.java          # CORS filter
│   ├── WebConfig.java                 # Web configuration
│   ├── WebSocketConfig.java           # STOMP/WebSocket configuration
│   └── WebSocketAuthInterceptor.java  # WebSocket JWT authentication
├── controller/                # REST API endpoints (16 controllers)
│   ├── AIAssistantController.java
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ChatController.java
│   ├── ExportController.java
│   ├── InventoryController.java
│   ├── InventoryHistoryController.java
│   ├── NotificationController.java
│   ├── PresenceRestController.java
│   ├── ProductController.java
│   ├── StatsController.java
│   ├── SupplierController.java
│   ├── TransactionController.java
│   └── UserController.java
├── dto/                       # Data Transfer Objects (20 DTOs)
│   ├── AIQueryRequest.java
│   ├── AIQueryResponse.java
│   ├── CategoryDTO.java
│   ├── ChatDTO.java
│   ├── ChatMessageDTO.java
│   ├── CompanyDTO.java
│   ├── CompanyRegistrationRequest.java
│   ├── CreateChatRequestDTO.java
│   ├── InventoryDTO.java
│   ├── InventoryHistoryDTO.java
│   ├── JwtResponse.java
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   ├── NotificationDTO.java
│   ├── ProductDTO.java
│   ├── SendMessageRequestDTO.java
│   ├── StatsSnapshotDTO.java
│   ├── SupplierDTO.java
│   ├── TransactionDTO.java
│   └── UserDTO.java
├── model/                     # JPA Entities (13 models)
│   ├── Category.java
│   ├── Chat.java
│   ├── ChatMessage.java
│   ├── ChatParticipant.java
│   ├── Company.java
│   ├── Inventory.java
│   ├── InventoryHistory.java
│   ├── Notification.java
│   ├── Product.java
│   ├── StatsSnapshot.java
│   ├── Supplier.java
│   ├── Transaction.java
│   └── User.java
├── repository/                # Spring Data JPA Repositories (13 repos)
├── security/                  # Authentication & Authorization
│   ├── AuthEntryPointJwt.java         # 401 Unauthorized handler
│   ├── JwtAuthenticationFilter.java   # JWT token filter
│   ├── JwtUtils.java                  # JWT generation/validation (HS256)
│   ├── SecurityConfig.java            # Security chain configuration
│   ├── SecurityUtils.java             # Auth context helpers
│   ├── UserDetailsImpl.java           # UserDetails implementation
│   └── UserDetailsServiceImpl.java    # User loading service
├── service/                   # Business logic
│   ├── impl/                          # Service implementations (10 classes)
│   ├── AIAssistantService.java
│   ├── CategoryService.java
│   ├── ChatService.java
│   ├── EncryptionService.java
│   ├── EntityBroadcastService.java    # Real-time entity update broadcasts
│   ├── ExportService.java
│   ├── InventoryHistoryService.java
│   ├── InventoryService.java
│   ├── NotificationService.java
│   ├── ProductService.java
│   ├── SupplierService.java
│   ├── TransactionService.java
│   ├── UserPresenceService.java       # Online/offline tracking
│   └── UserService.java
└── websocket/                 # WebSocket handlers
    ├── PresenceController.java        # Heartbeat message mapping
    └── WebSocketEventListener.java    # Connect/disconnect events
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Login with email/password, returns JWT |
| `POST` | `/api/auth/register-company` | Register new company + admin user |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | Get all products (company-scoped) |
| `GET` | `/api/products/{id}` | Get product by ID |
| `GET` | `/api/products/sku/{sku}` | Get product by SKU |
| `GET` | `/api/products/category/{categoryId}` | Get products by category |
| `GET` | `/api/products/supplier/{supplierId}` | Get products by supplier |
| `GET` | `/api/products/search?name=X` | Search products by name |
| `GET` | `/api/products/price?maxPrice=X` | Get products by max price |
| `POST` | `/api/products` | Create product (ADMIN/MANAGER) |
| `PUT` | `/api/products/{id}` | Update product (ADMIN/MANAGER) |
| `DELETE` | `/api/products/{id}` | Delete product (ADMIN/MANAGER) |

### Inventory
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/inventory` | Get all inventory |
| `GET` | `/api/inventory/{id}` | Get inventory by ID |
| `GET` | `/api/inventory/product/{productId}` | Get inventory by product |
| `GET` | `/api/inventory/low-stock` | Get low stock items |
| `GET` | `/api/inventory/check-stock?productId=X&quantity=Y` | Check stock availability |
| `POST` | `/api/inventory` | Create inventory record |
| `PUT` | `/api/inventory/{id}` | Update inventory |
| `PUT` | `/api/inventory/update-quantity/{productId}` | Update quantity |
| `DELETE` | `/api/inventory/{id}` | Delete inventory |

### Inventory History
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/inventory-history` | Get recent history (100 records) |
| `GET` | `/api/inventory-history/recent` | Alias for above |
| `POST` | `/api/inventory-history/record` | Record current inventory state |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/categories` | Get all categories |
| `GET` | `/api/categories/{id}` | Get category by ID |
| `POST` | `/api/categories` | Create category (ADMIN/MANAGER) |
| `PUT` | `/api/categories/{id}` | Update category (ADMIN/MANAGER) |
| `DELETE` | `/api/categories/{id}` | Delete category (ADMIN/MANAGER) |

### Suppliers
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/suppliers` | Get all suppliers |
| `GET` | `/api/suppliers/{id}` | Get supplier by ID |
| `GET` | `/api/suppliers/search?name=X` | Search suppliers by name |
| `POST` | `/api/suppliers` | Create supplier (ADMIN/MANAGER) |
| `PUT` | `/api/suppliers/{id}` | Update supplier (ADMIN/MANAGER) |
| `DELETE` | `/api/suppliers/{id}` | Delete supplier (ADMIN/MANAGER) |

### Stats & Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/stats/summary` | Current stats (products, categories, suppliers, low stock, inventory) |
| `GET` | `/api/stats/previous` | Previous day snapshot (for % change calculation) |
| `GET` | `/api/stats/history` | Last 30 days of stats |
| `GET` | `/api/stats/inventory-history` | Inventory history data |
| `POST` | `/api/stats/snapshot` | Record current snapshot |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users` | Get all company users (ADMIN only) |
| `GET` | `/api/users/me` | Get current user profile |
| `PUT` | `/api/users/me` | Update current user profile |
| `PUT` | `/api/users/me/password` | Change password |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/{id}` | Update user |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/chats/users` | Get available users in company |
| `GET` | `/api/chats` | Get all user chats |
| `GET` | `/api/chats/recent` | Get recent chats |
| `GET` | `/api/chats/{id}` | Get chat by ID |
| `GET` | `/api/chats/{id}/messages` | Get chat messages |
| `GET` | `/api/chats/unread-count` | Get unread message count |
| `POST` | `/api/chats` | Create new chat |
| `POST` | `/api/chats/{id}/messages` | Send message |
| `PUT` | `/api/chats/{id}/read` | Mark chat as read |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/notifications` | Get all notifications |
| `GET` | `/api/notifications/unread` | Get unread notifications |
| `GET` | `/api/notifications/count-unread` | Get unread count |
| `PUT` | `/api/notifications/{id}/read` | Mark as read |
| `PUT` | `/api/notifications/mark-all-read` | Mark all as read |
| `DELETE` | `/api/notifications/{id}` | Delete notification |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/transactions` | Get all transactions |
| `GET` | `/api/transactions/{id}` | Get transaction by ID |
| `GET` | `/api/transactions/product/{productId}` | Get by product |
| `GET` | `/api/transactions/type/{type}` | Get by type (PURCHASE, SALE, RETURN, ADJUSTMENT, TRANSFER) |
| `GET` | `/api/transactions/date-range?start=X&end=Y` | Get by date range |
| `POST` | `/api/transactions` | Create transaction |
| `DELETE` | `/api/transactions/{id}` | Delete transaction |

### AI Assistant
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/ai/ask` | Ask a natural language question about inventory |
| `GET` | `/api/ai/health` | AI service health check |

### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/export/excel` | Export all data to Excel (XLSX) |
| `GET` | `/api/export/products` | Export products |
| `GET` | `/api/export/suppliers` | Export suppliers |
| `GET` | `/api/export/inventory` | Export inventory |
| `GET` | `/api/export/categories` | Export categories |

### Presence (Online/Offline Status)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/presence/online` | Get all online users in company |
| `GET` | `/api/presence/user/{userId}` | Get specific user's presence |
| `POST` | `/api/presence/heartbeat` | Update user's last seen timestamp |

### WebSocket Endpoints

| Endpoint | Description |
|----------|-------------|
| `ws://localhost:8080/ws` | STOMP endpoint with SockJS fallback |
| `ws://localhost:8080/ws-raw` | Raw WebSocket endpoint |

**STOMP Topics:**

| Topic | Description |
|-------|-------------|
| `/topic/presence/{companyId}` | Real-time online/offline status updates |
| `/topic/updates/{companyId}` | Real-time entity updates (CREATE/UPDATE/DELETE) |

**STOMP Message Mappings:**

| Destination | Description |
|-------------|-------------|
| `/app/presence/heartbeat` | Client heartbeat to maintain online status |

## Database Schema

### Core Entities
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Company** | Multi-tenant isolation | id, name, cui, createdAt |
| **User** | Users with roles | id, name, email, password, role, company, lastSeen |
| **Product** | Products | id, name, description, sku, price, category, supplier, company |
| **Category** | Product categories | id, name, description, products, company |
| **Supplier** | Product suppliers | id, name, contactName, email, phone, address, company |
| **Inventory** | Stock levels | id, product, quantity, reorderLevel, reorderQuantity, location, company |
| **Transaction** | Stock movements | id, product, type, quantity, unitPrice, totalAmount, date, user, reference, company |

**Transaction Types:** `PURCHASE`, `SALE`, `RETURN`, `ADJUSTMENT`, `TRANSFER`

### Analytics Entities
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **InventoryHistory** | Tracks inventory changes | id, timestamp, product, totalQuantity, quantityChange, changeReason, company |
| **StatsSnapshot** | Daily metric snapshots | id, snapshotDate, totalProducts, totalCategories, totalSuppliers, lowStockItems, totalInventoryQuantity, company |

### Communication Entities
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Chat** | Conversations between users | id, participants, company, createdAt, updatedAt |
| **ChatMessage** | Individual messages | id, chat, sender, content, timestamp, readStatus |
| **ChatParticipant** | Participant tracking | id, chat, user, lastReadMessageId, joinedAt |
| **Notification** | System notifications | id, title, message, type, entityType, entityId, iconName, colors, user, company, read |

### Cascade Deletes
- **User** deletion cascades to: notifications, messages, chat participants
- **Product** deletion cascades to: inventory history, transactions
- **Supplier/Category** deletion cascades to: product associations

## Configuration

### application.properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/inventory_management
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwtSecret=your_secret_key_min_32_chars
app.jwtExpirationMs=604800000   # 7 days

# CORS
spring.web.cors.allowed-origins=http://localhost:3000

# Ollama AI (optional)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.1
spring.ai.ollama.chat.options.temperature=0.7

# Chat Encryption
app.encryption.key=YourSecureEncryptionKey32Bytes!
```

## Running the Application

### Prerequisites
- Java 21
- PostgreSQL
- Maven 3.6+
- Ollama (optional, for AI features)

### Steps

1. **Create database**
   ```sql
   CREATE DATABASE inventory_management;
   ```

2. **Configure application.properties**
   - Update database credentials
   - Set a secure JWT secret (minimum 32 characters)
   - Set a secure encryption key for chat messages

3. **Build the project**
   ```bash
   mvn clean package
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   The server starts on `http://localhost:8080`. Hibernate auto-creates all tables on first run.

5. **Optional: Setup AI Assistant**
   ```bash
   # Install Ollama from https://ollama.ai
   ollama pull llama3.1
   ```

## Security

### Authentication
- **Stateless JWT** authentication (HS256 algorithm)
- Token sent via `Authorization: Bearer {token}` header
- Token contains: user ID, company ID, company name, roles
- Token expiration: 7 days (configurable)

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access to all features, user management |
| **MANAGER** | CRUD on products, inventory, suppliers, categories + transactions |
| **EMPLOYEE** | Read access to all resources + create transactions |

### Multi-tenancy
All data is scoped to the user's company. Every query is filtered by `companyId` extracted from the JWT token, ensuring complete data isolation between companies.

### WebSocket Security
WebSocket connections are authenticated via JWT token passed in the STOMP `Authorization` header during the CONNECT frame.

## Scheduled Tasks

| Task | Schedule | Description |
|------|----------|-------------|
| Daily Stats Snapshot | Midnight (`0 0 0 * * ?`) | Records current metrics for all companies (products, categories, suppliers, low stock count, total inventory quantity) |

## Key Features

1. **Multi-tenant Architecture** - Complete data isolation between companies via company-scoped queries
2. **Real-time Updates via WebSocket** - Instant entity CREATE/UPDATE/DELETE broadcasts to `/topic/updates/{companyId}`
3. **User Presence System** - Real-time online/offline tracking using in-memory ConcurrentHashMap with heartbeat support
4. **Real-time Inventory Tracking** - Automatic history recording on all inventory changes
5. **Low Stock Alerts** - Identifies items below reorder level
6. **Analytics Dashboard** - Stats with historical comparison (30-day history, daily snapshots, % change)
7. **AI Assistant** - Natural language queries about inventory powered by Ollama/Llama 3.1 with intelligent context building
8. **Excel Export** - Export data to XLSX spreadsheets (all data or individual entity types)
9. **Chat System** - Internal messaging between company users with message encryption
10. **Transaction Tracking** - Complete audit trail of stock movements (PURCHASE, SALE, RETURN, ADJUSTMENT, TRANSFER)
11. **Notification System** - System notifications for events with read/unread tracking and styled icons
12. **Cascade Deletes** - Clean removal of related data when parent entities are deleted

## Future Enhancements

- [ ] Purchase Orders
- [ ] Barcode/QR Code Support
- [ ] Email Notifications
- [ ] Advanced Reporting
- [ ] Multi-Warehouse Support
- [ ] Batch/Lot Tracking
