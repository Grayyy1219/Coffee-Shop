package app.util;

import app.model.MenuItem;
import app.model.Order;
import app.model.OrderQueue;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Lightweight self-test runner (no external test framework required).
 */
public final class AlgorithmRequirementsSelfTest {

    public static void main(String[] args) {
        linkedListQueueSupportsEnqueueDequeueAndTraversal();
        linearSearchFindsOrdersAndMenuItems();
        insertionSortOrdersMenuItemsByPrice();
        System.out.println("Algorithm requirements self-test passed.");
    }

    private static void linkedListQueueSupportsEnqueueDequeueAndTraversal() {
        OrderQueue queue = new OrderQueue();
        Order first = buildOrder("A001", "Alex");
        Order second = buildOrder("A002", "Brooke");
        Order third = buildOrder("A003", "Casey");

        assertTrue(queue.enqueue(first), "enqueue should accept first order");
        assertTrue(queue.enqueue(second), "enqueue should accept second order");
        assertTrue(queue.enqueue(third), "enqueue should accept third order");

        List<Order> traversed = queue.traverse();
        assertEquals(Arrays.asList(first, second, third), traversed, "traversal should keep FIFO order");

        Order dequeued = queue.dequeue();
        assertEquals(first, dequeued, "dequeue should return first order");
        assertEquals(Arrays.asList(second, third), queue.traverse(), "queue should shift after dequeue");
        assertTrue(!queue.isEmpty(), "queue should not be empty after one dequeue");
    }

    private static void linearSearchFindsOrdersAndMenuItems() {
        Order first = buildOrder("B101", "Taylor");
        Order second = buildOrder("B102", "Jordan");
        OrderQueue queue = new OrderQueue();
        queue.enqueue(first);
        queue.enqueue(second);

        List<Order> orderMatches = LinearSearch.searchOrders(queue.traverse(), "tay", "B101");
        assertEquals(1, orderMatches.size(), "searchOrders should return one match");
        assertEquals(first, orderMatches.get(0), "searchOrders should return matching order");

        List<MenuItem> menu = Arrays.asList(
                new MenuItem("LAT01", "Latte", "Coffee", new BigDecimal("4.50")),
                new MenuItem("ESP01", "Espresso", "Coffee", new BigDecimal("3.25")),
                new MenuItem("TEA01", "Green Tea", "Tea", new BigDecimal("3.75"))
        );

        List<MenuItem> menuMatches = LinearSearch.searchMenuByName(menu, "lat");
        assertEquals(1, menuMatches.size(), "searchMenuByName should return one match");
        assertEquals("Latte", menuMatches.get(0).getName(), "searchMenuByName should match by name");
    }

    private static void insertionSortOrdersMenuItemsByPrice() {
        List<MenuItem> menu = Arrays.asList(
                new MenuItem("LAT01", "Latte", "Coffee", new BigDecimal("4.50")),
                new MenuItem("ESP01", "Espresso", "Coffee", new BigDecimal("3.25")),
                new MenuItem("TEA01", "Green Tea", "Tea", new BigDecimal("3.75"))
        );

        InsertionSort.sort(menu, Comparator.comparing(MenuItem::getPrice));

        assertEquals("ESP01", menu.get(0).getCode(), "insertion sort should order by price ascending");
        assertEquals("TEA01", menu.get(1).getCode(), "insertion sort should order by price ascending");
        assertEquals("LAT01", menu.get(2).getCode(), "insertion sort should order by price ascending");
    }

    private static Order buildOrder(String code, String customer) {
        Order order = new Order();
        order.setCode(code);
        order.setCustomerName(customer);
        return order;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }
}
