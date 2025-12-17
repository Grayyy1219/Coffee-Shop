package app.model;

/**
 * Simple singly-linked list node used by the custom queue.
 */
public class Node<T> {
    public T data;
    public Node<T> next;

    public Node(T data) {
        this.data = data;
    }
}
