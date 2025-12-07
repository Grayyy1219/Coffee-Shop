Scenario 1: Staff Logs Into the System
Description

A staff member opens the Coffee Shop System. They must enter their username and password to access the system dashboard.

Trigger

Staff opens the system application.

Actors

Staff / Owner

System (Login Module)

Flow

Staff opens the application.

Swing login window appears.

Staff enters username and password.

System sends a query to MySQL to validate credentials.

If credentials match:

The system loads the dashboard.

Displays modules: Menu, Orders, Queue, Reports.

If credentials fail:

System shows "Invalid Login" message.

User is allowed to retry.

If user cancels:

System closes.

Scenario 2: Customer Places an Order (Enqueue Order)
Description

The staff creates a new customer order, which is stored in the database and simultaneously added to the Queue (Linked List).

Trigger

A customer approaches the counter to order.

Actors

Customer

Staff

System (Order Module + Queue)

Flow

Staff selects “Create New Order”.

Swing form opens displaying the menu items.

Staff selects items and quantities.

System calculates the subtotal.

Staff confirms the order.

System checks:

If queue size < 50 → continue

If queue size = 50 → show “Queue is full, cannot accept order”

System inserts the order into the MySQL orders table.

System creates a new Order object in memory.

System enqueue() the order into the Linked List Queue:

Create new node

Add node to the tail of the queue

Queue now shows the updated order list in the GUI.

Customer pays or pays later depending on your design.

Scenario 3: Barista Processes the Next Order (Dequeue Order)
Description

The barista completes the oldest pending order. That order is removed from the queue and marked as completed in the database.

Trigger

Barista presses “Serve Next Order”.

Actors

Barista / Staff

System (Queue + Database)

Flow

Staff selects “Serve Next Order”.

System checks if queue is empty.

If empty:

Show message “No orders to serve.”

Stop flow.

System performs dequeue():

Remove node from the head of the queue

Retrieve order details

System updates MySQL:

Set order status = “Completed” or “Served”

System displays the order details (for confirmation).

System refreshes the GUI queue list.

Barista prepares for next order.

Scenario 4: Staff Searches for a Menu Item (Linear Search)
Description

Staff wants to check if a certain menu item exists.

Trigger

Staff clicks “Search Menu Item”.

Actors

Staff

System (Search Module)

Flow

Staff enters item name or item code.

System loads menu items into a temporary array/list (from DB or memory).

System performs Linear Search:

Loop through each item

Compare input with the item’s name/code

If found:

Display item details

If not found:

Show “Item not found” message

Staff returns to the menu screen.

Scenario 5: Staff Sorts Menu Items by Price (Insertion Sort)
Description

A manager wants to see the menu sorted by price ascending.

Trigger

Manager clicks “Sort Menu Items”.

Actors

Manager / Staff

System (Sorting Module)

Flow

Staff selects sorting criteria (e.g., “Sort by Price”).

System retrieves all menu items from MySQL.

System places menu items into an array/list.

System runs Insertion Sort on the list:

Starting from index 1

Store the key

Shift elements that are greater

Insert key in correct position

Sorted data is displayed in the Swing table.

Optionally: System allows staff to save sorting order preferences.

Scenario 6: Daily Sales Report Generation
Description

Owner wants to view total sales for the day.

Trigger

Owner clicks “Generate Daily Report”.

Actors

Owner

System (Report + Database Module)

Flow

Owner selects date or “Today’s Sales”.

System queries MySQL:

Get all completed orders for that date

System computes:

Total sales

Number of transactions

Top-selling items

System displays the results in a Swing window.

Owner can export as PDF or print (optional).

Scenario 7: Queue Reaches Maximum Capacity
Description

The system prevents new orders from being added when queue size = 50.

Trigger

Staff tries to create a new order when shop is very busy.

Actors

Customer

Staff

System

Flow

Staff selects “Create New Order”.

System checks current queue size.

If queue size = 50:

Display “Queue Full: Cannot Accept New Orders”

Order creation form is disabled or closed

Staff informs the customer to wait.

No new order object is created.

No database insertion occurs.

Scenario 8: Failed Login Attempt
Description

User enters incorrect credentials.

Trigger

User attempts login.

Actors

Staff

System

Flow

User enters username + password.

System checks MySQL.

Credentials do NOT match:

System shows “Incorrect username or password.”

User gets three tries (optional).

After 3 failed attempts:

System locks for 30 seconds (optional security feature).

Scenario 9: Menu Item Update (Edit Item)
Description

Owner wants to change price or availability of a menu item.

Flow

Staff selects menu item from table.

Clicks “Edit Item”.

Updates name, price, or category.

Clicks “Save Changes”.

System validates inputs (no empty fields, no negative prices).

System updates MySQL with new values.

GUI refreshes with updated menu.

Staff returns to previous screen.

Scenario 10: System Initialization
Description

When the software starts, it loads necessary resources.

Flow

Application starts.

System connects to local MySQL database.

System loads:

Menu items

Pending orders

System reconstructs the Linked List Queue from orders still marked as "Pending".

System opens the login screen.