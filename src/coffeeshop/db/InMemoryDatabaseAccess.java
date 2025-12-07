package coffeeshop.db;

import coffeeshop.model.Order;
import coffeeshop.model.OrderStatus;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDatabaseAccess implements DatabaseAccess {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> roles = new HashMap<>();
    private final Map<String, Order> orders = new HashMap<>();

    public InMemoryDatabaseAccess() {
        users.put("admin", "admin123");
        roles.put("admin", "owner");
        users.put("cashier", "cashier123");
        roles.put("cashier", "cashier");
        users.put("barista", "barista123");
        roles.put("barista", "barista");
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
}
