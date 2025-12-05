package coffeeshop.queue;

import coffeeshop.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderQueue {
    private static final int MAX_ORDERS = 50;
    private Node<Order> head;
    private Node<Order> tail;
    private int size;

    public void enqueue(Order order) {
        if (isFull()) {
            throw new IllegalStateException("Order queue is full (50 active orders).");
        }
        Node<Order> newNode = new Node<>(order);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public Order dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("No orders to process.");
        }
        Order order = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return order;
    }

    public List<Order> traverse() {
        List<Order> orders = new ArrayList<>();
        Node<Order> current = head;
        while (current != null) {
            orders.add(current.data);
            current = current.next;
        }
        return orders;
    }

    public boolean isFull() {
        return size >= MAX_ORDERS;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
