package coffeeshop.db;

import coffeeshop.model.Order;
import coffeeshop.model.OrderStatus;
import coffeeshop.model.MenuItem;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDatabaseAccess implements DatabaseAccess {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> roles = new HashMap<>();
    private final Map<String, Order> orders = new HashMap<>();
    private final List<MenuItem> menuItems = new ArrayList<>();

    public InMemoryDatabaseAccess() {
        users.put("admin", "admin123");
        roles.put("admin", "owner");
        users.put("cashier", "cashier123");
        roles.put("cashier", "cashier");
        users.put("barista", "barista123");
        roles.put("barista", "barista");

        menuItems.add(new MenuItem("CF001", "Latte", "Coffee", 4.50));
        menuItems.add(new MenuItem("CF002", "Espresso", "Coffee", 3.00));
        menuItems.add(new MenuItem("PT101", "Chocolate Croissant", "Pastry", 3.25));
    }

    @Override
    public boolean validateUser(String username, String password) throws SQLException {
        return password.equals(users.get(username));
    }

    @Override
    public String getUserRole(String username) throws SQLException {
        return roles.get(username);
    }

    @Override
    public List<MenuItem> loadMenuItems() throws SQLException {
        return new ArrayList<>(menuItems);
    }

    @Override
    public void saveOrder(Order order) throws SQLException {
        orders.put(order.getOrderId(), order);
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus status, boolean paid) throws SQLException {
        Order order = orders.get(orderId);
        if (order == null) {
            return;
        }
        switch (status) {
            case SERVED -> order.markServed();
            case PAID -> order.markPaid();
            default -> { }
        }
    }

    @Override
    public List<Order> loadActiveOrders() throws SQLException {
        return new ArrayList<>(orders.values());
    }

    @Override
    public List<Order> loadAllOrders() throws SQLException {
        return new ArrayList<>(orders.values());
    }
}
