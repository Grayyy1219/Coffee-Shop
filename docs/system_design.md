# Coffee Shop Management System – Offline Java/Swing + MySQL Design

## 1. High-Level System Overview
**Description:** A desktop Java Swing application (built in NetBeans) that runs entirely offline against a local MySQL instance. It supports staff/owner operations for menu management, order taking, order processing, billing, and reporting. Customers are served indirectly by staff entering orders.

**Main modules**
- **Login Module:** Swing login form; verifies username/password against the local MySQL `users` table via JDBC. Blocks all other modules until authentication succeeds.
- **Menu Management Module:** CRUD for menu items (coffee, pastries, etc.) stored in `menu_items`. Provides sorted views (by name/price/category) using insertion sort on in-memory data.
- **Order Management (Queue) Module:** Uses a custom Linked List–backed Queue to hold up to **50 active orders** (FIFO). Supports enqueue (new order), dequeue (serve next), traverse (list active queue), and linear search of active orders.
- **Billing/Payment Module:** Calculates totals, records payments, and updates order status in MySQL. Integrates with dequeue flow to mark served/paid.
- **Reports/History Module:** Loads historical orders/sales from MySQL, provides search (linear) and sorting (insertion sort) for lists such as daily sales.

**Offline handling:** All logic and data persist locally. Swing UI talks only to the local MySQL server (embedded/installed on the same machine). No network calls are required.

## 2. Database Model (Conceptual)
Tables (minimum):
- `users(user_id, username, password_hash, role)`
- `menu_items(item_id, code, name, category, price, status)`
- `orders(order_id, customer_name, order_time, status, total_amount)`
- `order_items(order_item_id, order_id, item_id, quantity, line_total)`
- `sales(sale_id, order_id, paid_time, total_amount, payment_method)`

**Data interaction:**
- On login/menu/order flows, records are retrieved via JDBC into in-memory objects (e.g., `List<MenuItem>` or queue of `Order`).
- Queue nodes reference `Order` objects loaded/created in memory; status changes are persisted back to `orders`/`sales` tables.
- Sorting and searching operate on in-memory collections (arrays/lists) populated from the database; after reorder, the UI displays the sorted collection (DB order unaffected).

## 3. Data Structures & Core Classes (Conceptual)
- `class User { int id; String username; String passwordHash; String role; }`
- `class MenuItem { int id; String code; String name; String category; double price; String status; }`
- `class OrderItem { MenuItem item; int quantity; double lineTotal; }`
- `class Order { int id; String customerName; LocalDateTime orderTime; String status; double totalAmount; List<OrderItem> items; }`
- `class Node { Order data; Node next; }` (for the linked list queue)
- `class OrderQueue {`
  - `Node front, rear; int size; final int MAX_SIZE = 50;`
  - `enqueue(Order order)`, `dequeue()`, `traverse()`, `isFull()`, `isEmpty()`
  - Uses linked list nodes; enforces `MAX_SIZE` before adding.
- `class OrderManager { OrderQueue activeQueue; DatabaseManager db; }`
  - Handles creating orders, pushing to queue, persisting to DB, linear search on queue/history.
- `class DatabaseManager { Connection getConnection(); CRUD for users/menu/orders; }`
- Utility functions: `linearSearchOrder(List<Order> list, predicate)`, `linearSearchMenu(List<MenuItem> list, predicate)`, `insertionSortMenu(List<MenuItem> list, Comparator)`, `insertionSortSales(List<Sale> list, Comparator)`.

## 4. Detailed Step-by-Step Algorithms (Pseudo-code)

### 4.1 User Login (Swing form + MySQL)
```
Input: usernameInput, passwordInput
On Login Button Click:
  connection = DatabaseManager.getConnection()
  result = SELECT password_hash FROM users WHERE username = usernameInput
  IF result is empty THEN
      show "Invalid username/password"
      return
  ENDIF
  IF hash(passwordInput) != result.password_hash THEN
      show "Invalid username/password"
      return
  ENDIF
  open MainDashboard()
```

### 4.2 Add New Menu Item (DB insert)
```
Input: code, name, category, price
Validate fields (non-empty, price > 0)
connection = DatabaseManager.getConnection()
EXECUTE INSERT INTO menu_items(code, name, category, price, status) VALUES(?,?,?,?, 'ACTIVE')
IF insert success THEN
    show "Menu item saved"
    refreshMenuListFromDB()
ELSE
    show error message
ENDIF
```

### 4.3 Take Customer Order + Enqueue (Linked List Queue)
```
Input: customerName, selectedMenuItems (with qty)
Compute totalAmount from item prices
Create Order object (status='PENDING', orderTime=now, items=selectedMenuItems)
connection = DatabaseManager.getConnection()
INSERT order into orders table (return order_id)
INSERT each order_item into order_items table (order_id FK)
Set Order.id = generated order_id
// Enqueue to active queue
IF OrderQueue.isFull() THEN
    show "Queue full (50 active orders). Cannot take new orders."
    return
ENDIF
OrderQueue.enqueue(Order)
show "Order added to queue"
```

### 4.4 Serve/Process Order (Dequeue + DB update + Payment)
```
IF OrderQueue.isEmpty() THEN
    show "No active orders"
    return
ENDIF
order = OrderQueue.dequeue()  // FIFO
Display order details for payment
Receive payment_method and confirmation
connection = DatabaseManager.getConnection()
UPDATE orders SET status='SERVED', total_amount=order.totalAmount WHERE order_id=order.id
INSERT INTO sales(order_id, paid_time, total_amount, payment_method) VALUES(order.id, now, order.totalAmount, payment_method)
show "Order served and paid"
```

### 4.5 Search Order (Linear Search in queue or history)
```
Function searchOrderById(targetId):
  list = OrderQueue.traverse()   // returns List<Order>
  FOR i FROM 0 TO list.size-1
     IF list[i].id == targetId THEN return list[i]
  END FOR
  return null
```
For history, load `orders` rows into a list first, then perform the same linear scan.

### 4.6 Search Menu Item (Linear Search)
```
Function searchMenuByName(targetName):
  list = loadMenuFromDB()
  FOR each item IN list
     IF item.name.equalsIgnoreCase(targetName) THEN return item
  END FOR
  return null
```

### 4.7 Sort Menu Items or Sales Records (Insertion Sort)
```
Function insertionSortMenuByPrice(List<MenuItem> list):
  FOR i FROM 1 TO list.size-1
     key = list[i]
     j = i - 1
     WHILE j >= 0 AND list[j].price > key.price
         list[j+1] = list[j]
         j = j - 1
     END WHILE
     list[j+1] = key
  END FOR
  return list
```
Trigger this when the user selects "Sort by Price/Name" in the UI. The list is prepared by loading menu records from MySQL into an ArrayList, then sorted in-memory and displayed.

## 5. Flow Diagrams (Mermaid)

### 5.1 Main System Flow
```mermaid
flowchart TD
    A[Start App] --> B[Load Login Form]
    B --> C{Login Success?}
    C -- No --> B
    C -- Yes --> D[Load Menu + Queue Data from DB]
    D --> E[Dashboard]
    E --> F[Menu Management]
    E --> G[Take Order]
    E --> H[Process Order]
    E --> I[Reports]
    F --> E
    G --> E
    H --> E
    I --> E
    E --> Z[Exit]
```

### 5.2 Login Flow
```mermaid
flowchart TD
    L1[Enter username/password] --> L2[Query users table]
    L2 --> L3{Record found?}
    L3 -- No --> L4[Show invalid message]
    L4 --> L1
    L3 -- Yes --> L5{Password hash matches?}
    L5 -- No --> L4
    L5 -- Yes --> L6[Open Dashboard]
```

### 5.3 Take Order (Enqueue)
```mermaid
flowchart TD
    T1[Staff selects items + qty] --> T2[Calculate total]
    T2 --> T3[Create Order object]
    T3 --> T4[Insert order + items into MySQL]
    T4 --> T5{Queue full? size>=50}
    T5 -- Yes --> T6[Show queue full error]
    T5 -- No --> T7[OrderQueue.enqueue(order)]
    T7 --> T8[Confirm order added]
```

### 5.4 Process/Serve Order (Dequeue)
```mermaid
flowchart TD
    P1[Click Serve Next] --> P2{Queue empty?}
    P2 -- Yes --> P3[Show "No active orders"]
    P2 -- No --> P4[OrderQueue.dequeue()]
    P4 --> P5[Display order details]
    P5 --> P6[Receive payment method]
    P6 --> P7[Update orders status in DB]
    P7 --> P8[Insert sales record]
    P8 --> P9[Show success / print receipt]
```

### 5.5 Search Order (Linear Search)
```mermaid
flowchart TD
    S1[Enter order ID/name] --> S2[Load active queue list]
    S2 --> S3[Linear scan from front to rear]
    S3 --> S4{Match found?}
    S4 -- Yes --> S5[Show order details]
    S4 -- No --> S6[Show "Not found"]
```

### 5.6 Sort Records (Insertion Sort)
```mermaid
flowchart TD
    R1[User clicks Sort by Price] --> R2[Load menu list from DB]
    R2 --> R3[Run insertion sort on list]
    R3 --> R4[Display sorted list in UI]
```

## 6. Assumptions & Constraints
- Single workstation/user at a time; no concurrent queue modifications assumed.
- Local MySQL service available; JDBC URL points to localhost.
- Queue holds **max 50 active orders**; new orders rejected when full.
- `dequeue` on empty queue shows an error.
- Invalid login credentials show a message; account lockout is optional.
- Linear search is used for order/menu lookup; insertion sort is used for UI sorting only (does not change DB order).
- Swing UI handles input validation and error prompts.

## 7. Possible Improvements (optional)
- Add receipt printing and export-to-CSV for sales reports.
- Add configurable queue limit and roles/permissions per user.
- Add caching layer for menu data to reduce DB hits on read.
