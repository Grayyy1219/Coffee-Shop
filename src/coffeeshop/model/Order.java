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
    private final double total;
    private final List<OrderItem> items;

    public Order(String orderId, String customerName, List<OrderItem> items) {
        this(orderId, customerName, items, LocalDateTime.now(), OrderStatus.PENDING, false,
                items.stream().mapToDouble(OrderItem::getLineTotal).sum());
    }

    public Order(String orderId,
                 String customerName,
                 List<OrderItem> items,
                 LocalDateTime createdAt,
                 OrderStatus status,
                 boolean paid,
                 double total) {
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.customerName = Objects.requireNonNullElse(customerName, "Walk-in");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items"));
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = Objects.requireNonNull(status, "status");
        this.paid = paid;
        this.total = total;
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
        return total;
    }

    public void markServed() {
        this.status = OrderStatus.SERVED;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paid = true;
    }
}
