package coffeeshop.order;

import coffeeshop.db.DatabaseAccess;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.model.OrderStatus;
import coffeeshop.queue.OrderQueue;
import coffeeshop.search.LinearSearch;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OrderManager {
    private final OrderQueue orderQueue;
    private final DatabaseAccess databaseAccess;

    public OrderManager(OrderQueue orderQueue, DatabaseAccess databaseAccess) {
        this.orderQueue = orderQueue;
        this.databaseAccess = databaseAccess;
    }

    public void loadActiveOrders() throws SQLException {
        orderQueue.clear();
        for (Order order : databaseAccess.loadActiveOrders()) {
            orderQueue.enqueue(order);
        }
    }

    public List<MenuItem> loadMenuItems() throws SQLException {
        return databaseAccess.loadMenuItems();
    }

    public List<Order> loadAllOrders() throws SQLException {
        return databaseAccess.loadAllOrders();
    }

    public void placeOrder(Order order) throws SQLException {
        if (orderQueue.isFull()) {
            throw new IllegalStateException("Cannot accept more than 50 active orders.");
        }
        databaseAccess.saveOrder(order);
        orderQueue.enqueue(order);
    }

    public Order processNextOrder() throws SQLException {
        Order order = orderQueue.dequeue();
        order.markServed();
        databaseAccess.updateOrderStatus(order.getOrderId(), OrderStatus.SERVED, order.isPaid());
        return order;
    }

    public void recordPayment(Order order) throws SQLException {
        order.markPaid();
        databaseAccess.updateOrderStatus(order.getOrderId(), OrderStatus.PAID, true);
    }

    public Optional<Order> findOrderById(String orderId) {
        return LinearSearch.findOrderById(orderQueue, orderId);
    }

    public Optional<Order> findOrderByCustomer(String name) {
        return LinearSearch.findOrderByCustomer(orderQueue, name);
    }

    public List<Order> getActiveOrders() {
        return orderQueue.traverse();
    }

    public int getActiveOrderCount() {
        return orderQueue.size();
    }
}
