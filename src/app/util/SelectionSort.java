package app.util;

import java.util.Comparator;
import java.util.List;

/**
 * In-place selection sort helper for small collections used by the UI.
 */
public final class SelectionSort {

    private SelectionSort() {}

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || comparator == null) return;

        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(list.get(j), list.get(min)) < 0) {
                    min = j;
                }
            }
            if (min != i) {
                T temp = list.get(i);
                list.set(i, list.get(min));
                list.set(min, temp);
            }
        }
    }
}
