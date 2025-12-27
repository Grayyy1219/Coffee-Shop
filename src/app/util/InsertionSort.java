package app.util;

import java.util.Comparator;
import java.util.List;

/**
 * In-place insertion sort helper for small collections used by the UI.
 */
public final class InsertionSort {

    private InsertionSort() {}

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || comparator == null) {
            return;
        }

        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }
}
