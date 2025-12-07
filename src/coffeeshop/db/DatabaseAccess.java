package coffeeshop.db;

import coffeeshop.model.Order;
import coffeeshop.model.OrderStatus;
import coffeeshop.model.MenuItem;
import java.sql.SQLException;
import java.util.List;

public interface DatabaseAccess {
    boolean validateUser(String username, String password) throws SQLException;
    String getUserRole(String username) throws SQLException;
    List<MenuItem> loadMenuItems() throws SQLException;
    void saveOrder(Order order) throws SQLException;
    void updateOrderStatus(String orderId, OrderStatus status, boolean paid) throws SQLException;
    List<Order> loadActiveOrders() throws SQLException;
    List<Order> loadAllOrders() throws SQLException;
}
