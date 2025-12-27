package app.util;

import app.model.MenuItem;
import app.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Simple reusable linear search helper for menu items and orders.
 */
public final class LinearSearch {

    private LinearSearch() {}

    public static <T> List<T> search(Iterable<T> source, Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        if (source == null || predicate == null) {
            return results;
        }
        for (T item : source) {
            if (predicate.test(item)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Case-insensitive linear search across menu items by code, name, or category.
     */
    public static List<MenuItem> searchMenuByName(List<MenuItem> items, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return search(items, item ->
                safeLower(item.getCode()).contains(q)
                        || safeLower(item.getName()).contains(q)
                        || safeLower(item.getCategory()).contains(q));
    }

    /**
     * Linear search for orders by customer (and optional order code/id fragment).
     */
    public static List<Order> searchOrders(List<Order> orders, String customer, String orderCode) {
        String c = customer == null ? "" : customer.trim().toLowerCase(Locale.ROOT);
        String code = orderCode == null ? "" : orderCode.trim().toLowerCase(Locale.ROOT);
        return search(orders, order ->
                order.getCustomerName().toLowerCase(Locale.ROOT).contains(c)
                        && (code.isEmpty() || order.getCode().toLowerCase(Locale.ROOT).contains(code))
        );
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
