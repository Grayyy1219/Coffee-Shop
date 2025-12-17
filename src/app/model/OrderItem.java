package app.model;

import java.math.BigDecimal;

public class OrderItem {
    private String itemCode;
    private String itemName;
    private String optionsLabel;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOptionsLabel() {
        return optionsLabel == null ? "" : optionsLabel;
    }

    public void setOptionsLabel(String optionsLabel) {
        this.optionsLabel = optionsLabel;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
