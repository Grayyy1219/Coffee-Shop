package coffeeshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String orderId;
    private final String customerName;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private boolean paid;
    private final List<OrderItem> items;

    public Order(String orderId, String customerName, List<OrderItem> items) {
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.customerName = Objects.requireNonNullElse(customerName, "Walk-in");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items"));
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.paid = false;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isPaid() {
        return paid;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public void markServed() {
        this.status = OrderStatus.SERVED;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paid = true;
    }
}
