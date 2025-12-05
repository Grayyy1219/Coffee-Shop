package coffeeshop.search;

import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.queue.OrderQueue;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class LinearSearch {
    private LinearSearch() {}

    public static Optional<Order> findOrder(OrderQueue queue, Predicate<Order> predicate) {
        return queue.traverse().stream().filter(predicate).findFirst();
    }

    public static Optional<Order> findOrderById(OrderQueue queue, String orderId) {
        return findOrder(queue, order -> order.getOrderId().equalsIgnoreCase(orderId));
    }

    public static Optional<Order> findOrderByCustomer(OrderQueue queue, String customerName) {
        return findOrder(queue, order -> order.getCustomerName().equalsIgnoreCase(customerName));
    }

    public static Optional<MenuItem> findMenuItemByCode(List<MenuItem> menu, String code) {
        return menu.stream().filter(item -> item.getCode().equalsIgnoreCase(code)).findFirst();
    }

    public static Optional<MenuItem> findMenuItemByName(List<MenuItem> menu, String name) {
        return menu.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst();
    }
}
