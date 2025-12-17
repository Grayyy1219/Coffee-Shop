DROP DATABASE IF EXISTS coffee_shop;
CREATE DATABASE coffee_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE coffee_shop;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'staff'
);

INSERT INTO users (username, password, role) VALUES
    ('owner', 'owner123', 'owner'),
    ('barista', 'coffee!', 'staff'),
    ('cashier', 'payme', 'staff'),
    ('manager', 'beans4dayz', 'owner');

CREATE TABLE menu_items (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(8,2) NOT NULL
);

INSERT INTO menu_items (code, name, category, price) VALUES
    ('CF001', 'Espresso', 'Coffee', 3.00),
    ('CF002', 'Latte', 'Coffee', 4.25),
    ('CF003', 'Cappuccino', 'Coffee', 4.50),
    ('CF004', 'Cold Brew', 'Coffee', 4.75),
    ('CF005', 'Mocha', 'Coffee', 4.95),
    ('TE001', 'Chai Tea', 'Tea', 3.75),
    ('TE002', 'Green Tea', 'Tea', 3.25),
    ('TE003', 'Matcha Latte', 'Tea', 4.10),
    ('PA001', 'Butter Croissant', 'Pastry', 2.95),
    ('PA002', 'Blueberry Muffin', 'Pastry', 3.10),
    ('PA003', 'Almond Biscotti', 'Pastry', 2.65),
    ('FD001', 'Avocado Toast', 'Food', 6.50),
    ('FD002', 'Breakfast Sandwich', 'Food', 6.95),
    ('FD003', 'Bagel with Cream Cheese', 'Food', 3.95),
    ('FD004', 'Chicken Panini', 'Food', 8.50);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    item_code VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    options VARCHAR(100) DEFAULT '',
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item FOREIGN KEY (item_code) REFERENCES menu_items(code)
);

INSERT INTO orders (code, customer_name, status, subtotal, tax, total, paid, created_at) VALUES
    ('ORD-2001', 'Jane Doe', 'PENDING', 14.40, 1.30, 15.70, FALSE, '2024-05-01 08:30:00'),
    ('ORD-2002', 'Office Pickup', 'IN_PROGRESS', 18.80, 1.69, 20.49, FALSE, '2024-05-01 08:45:00'),
    ('ORD-2003', 'Carlos M.', 'COMPLETED', 14.35, 1.22, 15.57, TRUE, '2024-05-01 09:15:00'),
    ('ORD-2004', 'Walk-in', 'COMPLETED', 10.70, 0.96, 11.66, TRUE, '2024-05-02 10:00:00');

INSERT INTO order_items (order_id, item_code, item_name, options, quantity, unit_price, line_total) VALUES
    (1, 'CF005', 'Mocha', 'Oat milk', 1, 4.95, 4.95),
    (1, 'PA001', 'Butter Croissant', '', 1, 2.95, 2.95),
    (1, 'FD001', 'Avocado Toast', 'Extra chili flakes', 1, 6.50, 6.50),

    (2, 'CF002', 'Latte', 'Almond milk', 2, 4.25, 8.50),
    (2, 'TE003', 'Matcha Latte', '', 1, 4.10, 4.10),
    (2, 'PA002', 'Blueberry Muffin', 'Heated', 2, 3.10, 6.20),

    (3, 'CF004', 'Cold Brew', 'Light ice', 1, 4.75, 4.75),
    (3, 'FD002', 'Breakfast Sandwich', 'No cheese', 1, 6.95, 6.95),
    (3, 'PA003', 'Almond Biscotti', '', 1, 2.65, 2.65),

    (4, 'CF001', 'Espresso', 'Double shot', 1, 3.00, 3.00),
    (4, 'FD003', 'Bagel with Cream Cheese', 'Toasted', 1, 3.95, 3.95),
    (4, 'TE001', 'Chai Tea', '', 1, 3.75, 3.75);

CREATE OR REPLACE VIEW v_daily_sales AS
SELECT DATE(created_at) AS sale_date,
       SUM(total) AS gross_total,
       SUM(CASE WHEN paid THEN total ELSE 0 END) AS paid_total,
       COUNT(*) AS order_count
FROM orders
GROUP BY DATE(created_at)
ORDER BY sale_date;
