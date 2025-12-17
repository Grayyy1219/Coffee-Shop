package app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Linked-list backed FIFO queue for active orders.
 */
public class OrderQueue {
    public static final int MAX_SIZE = 50;

    private Node<Order> head;
    private Node<Order> tail;
    private int size;

    public boolean enqueue(Order order) {
        if (size >= MAX_SIZE) return false;
        Node<Order> node = new Node<>(order);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        return true;
    }

    public Order dequeue() {
        if (head == null) return null;
        Order data = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return data;
    }

    public Order peek() {
        return head == null ? null : head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<Order> traverse() {
        List<Order> out = new ArrayList<>();
        Node<Order> current = head;
        while (current != null) {
            out.add(current.data);
            current = current.next;
        }
        return out;
    }
}
