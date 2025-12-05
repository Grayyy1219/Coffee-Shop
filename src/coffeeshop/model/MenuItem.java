package coffeeshop.model;

import java.util.Objects;

public class MenuItem {
    private final String code;
    private final String name;
    private final String category;
    private final double price;

    public MenuItem(String code, String name, String category, double price) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.category = Objects.requireNonNullElse(category, "");
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
