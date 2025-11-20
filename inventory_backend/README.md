# Inventory Management System - Backend

A Spring Boot REST API for multi-tenant inventory management with real-time tracking, analytics, and AI assistant capabilities.

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.3**
- **PostgreSQL**
- **Spring Security + JWT**
- **Spring AI + Ollama** (AI Assistant)
- **Maven**

## Project Structure

```
src/main/java/com/example/inventory_backend/
├── config/                    # Configuration classes
│   └── ScheduledTasks.java    # Daily automated tasks
├── controller/                # REST API endpoints
│   ├── AIAssistantController.java
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ChatController.java
│   ├── ExportController.java
│   ├── InventoryController.java
│   ├── InventoryHistoryController.java
│   ├── NotificationController.java
│   ├── ProductController.java
│   ├── StatsController.java
│   ├── SupplierController.java
│   ├── TransactionController.java
│   └── UserController.java
├── dto/                       # Data Transfer Objects
│   ├── AIQueryRequest.java
│   ├── AIQueryResponse.java
│   ├── CategoryDTO.java
│   ├── ChatDTO.java
│   ├── ChatMessageDTO.java
│   ├── CompanyDTO.java
│   ├── InventoryDTO.java
│   ├── InventoryHistoryDTO.java
│   ├── ProductDTO.java
│   ├── StatsSnapshotDTO.java
│   ├── SupplierDTO.java
│   ├── TransactionDTO.java
│   └── UserDTO.java
├── model/                     # JPA Entities
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
├── repository/                # Spring Data JPA Repositories
├── security/                  # Security configuration
│   ├── AuthEntryPointJwt.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   ├── SecurityConfig.java
│   ├── SecurityUtils.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
└── service/                   # Business logic
    ├── impl/                  # Service implementations
    └── [Service Interfaces]
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register new company/user

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get by SKU
- `GET /api/products/category/{id}` - Get by category
- `GET /api/products/supplier/{id}` - Get by supplier
- `GET /api/products/search?name=X` - Search by name
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Inventory
- `GET /api/inventory` - Get all inventory
- `GET /api/inventory/{id}` - Get by ID
- `GET /api/inventory/product/{id}` - Get by product
- `GET /api/inventory/low-stock` - Get low stock items
- `PUT /api/inventory/update-quantity/{id}` - Update quantity
- `POST /api/inventory` - Create inventory
- `PUT /api/inventory/{id}` - Update inventory
- `DELETE /api/inventory/{id}` - Delete inventory

### Inventory History
- `GET /api/inventory-history` - Get recent history (100 records)
- `GET /api/inventory-history/recent` - Alias for above
- `POST /api/inventory-history/record` - Record current state

### Categories
- `GET /api/categories` - Get all
- `GET /api/categories/{id}` - Get by ID
- `POST /api/categories` - Create
- `PUT /api/categories/{id}` - Update
- `DELETE /api/categories/{id}` - Delete

### Suppliers
- `GET /api/suppliers` - Get all
- `GET /api/suppliers/{id}` - Get by ID
- `GET /api/suppliers/search?name=X` - Search
- `POST /api/suppliers` - Create
- `PUT /api/suppliers/{id}` - Update
- `DELETE /api/suppliers/{id}` - Delete

### Stats & Analytics
- `GET /api/stats/summary` - Current stats summary
- `GET /api/stats/previous` - Previous day stats (for % change)
- `GET /api/stats/history` - Last 30 days of stats
- `GET /api/stats/inventory-history` - Inventory history
- `POST /api/stats/snapshot` - Record current snapshot

### Users
- `GET /api/users/me` - Get current user
- `PUT /api/users/me` - Update current user profile
- `PUT /api/users/me/password` - Change password
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user (own profile only)
- `GET /api/users` - Get all users (Admin only)

### Chat
- `GET /api/chats` - Get all user chats
- `GET /api/chats/recent` - Get recent chats
- `GET /api/chats/{id}` - Get chat by ID
- `GET /api/chats/{id}/messages` - Get chat messages
- `GET /api/chats/unread-count` - Get unread count
- `POST /api/chats` - Create new chat
- `POST /api/chats/{id}/messages` - Send message
- `PUT /api/chats/{id}/read` - Mark as read

### Notifications
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/count-unread` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read

### Transactions
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get by ID
- `GET /api/transactions/product/{id}` - Get by product
- `GET /api/transactions/type/{type}` - Get by type
- `POST /api/transactions` - Create transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### AI Assistant
- `POST /api/ai/ask` - Ask a question about inventory
- `GET /api/ai/health` - Health check

### Export
- `GET /api/export/all` - Export all data to Excel
- `GET /api/export/products` - Export products
- `GET /api/export/suppliers` - Export suppliers
- `GET /api/export/inventory` - Export inventory
- `GET /api/export/categories` - Export categories

## Database Schema

### Core Entities
- **Company** - Multi-tenant isolation
- **User** - Users with roles (ADMIN, MANAGER, EMPLOYEE)
- **Product** - Products with SKU, price, category, supplier
- **Category** - Product categories
- **Supplier** - Product suppliers with contact info
- **Inventory** - Stock levels, reorder points, locations
- **Transaction** - Stock movements (PURCHASE, SALE, RETURN, ADJUSTMENT, TRANSFER)

### Analytics Entities
- **InventoryHistory** - Tracks all inventory changes with reasons
- **StatsSnapshot** - Daily snapshots of key metrics

### Communication Entities
- **Chat** - Chat conversations between users
- **ChatMessage** - Individual messages
- **ChatParticipant** - Chat participants with read status
- **Notification** - System notifications

## Configuration

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/inventory_management
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT
app.jwtSecret=your_secret_key
app.jwtExpirationMs=604800000

# Ollama AI (optional)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.1
```

## Running the Application

### Prerequisites
- Java 21
- PostgreSQL
- Maven
- Ollama (optional, for AI features)

### Steps

1. **Create database**
   ```sql
   CREATE DATABASE inventory_management;
   ```

2. **Configure application.properties**
   - Update database credentials
   - Set JWT secret

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Optional: Setup AI Assistant**
   ```bash
   # Install Ollama from https://ollama.ai
   ollama pull llama3.1
   ```

## Security

### Roles
- **ADMIN** - Full access to all features
- **MANAGER** - Can manage products, inventory, suppliers, categories
- **EMPLOYEE** - Read access + transactions

### Multi-tenancy
All data is scoped to the user's company. Users can only access data belonging to their company.

## Scheduled Tasks

- **Daily Stats Snapshot** - Runs at midnight, records current metrics for all companies

## Key Features

1. **Multi-tenant Architecture** - Complete data isolation between companies
2. **Real-time Inventory Tracking** - Automatic history recording on all changes
3. **Low Stock Alerts** - Identifies items below reorder level
4. **Analytics Dashboard** - Stats with historical comparison
5. **AI Assistant** - Natural language queries about inventory (requires Ollama)
6. **Excel Export** - Export data to spreadsheets
7. **Chat System** - Internal messaging between company users
8. **Transaction Tracking** - Complete audit trail of stock movements

## Future Enhancements

- [ ] Purchase Orders
- [ ] Barcode/QR Code Support
- [ ] Email Notifications
- [ ] Advanced Reporting
- [ ] Multi-Warehouse Support
- [ ] Batch/Lot Tracking
