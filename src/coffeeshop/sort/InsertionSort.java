package coffeeshop.sort;

import coffeeshop.model.MenuItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InsertionSort {
    private InsertionSort() {}

    public static List<MenuItem> sortMenu(List<MenuItem> input, Comparator<MenuItem> comparator) {
        List<MenuItem> sorted = new ArrayList<>(input);
        for (int i = 1; i < sorted.size(); i++) {
            MenuItem key = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(sorted.get(j), key) > 0) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, key);
        }
        return sorted;
    }

    public static List<MenuItem> sortByPrice(List<MenuItem> input) {
        return sortMenu(input, Comparator.comparingDouble(MenuItem::getPrice));
    }

    public static List<MenuItem> sortByName(List<MenuItem> input) {
        return sortMenu(input, Comparator.comparing(MenuItem::getName, String.CASE_INSENSITIVE_ORDER));
    }
}
