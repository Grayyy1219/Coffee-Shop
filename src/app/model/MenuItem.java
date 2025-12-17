package app.model;

import java.math.BigDecimal;

public class MenuItem {
    private String code;
    private String name;
    private String category;
    private BigDecimal price;

    public MenuItem(String code, String name, String category, BigDecimal price) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
}
