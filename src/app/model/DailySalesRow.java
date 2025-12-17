package app.model;

import java.math.BigDecimal;
import java.sql.Date;

public class DailySalesRow {
    public Date saleDate;
    public BigDecimal grossTotal = BigDecimal.ZERO;
    public BigDecimal paidTotal = BigDecimal.ZERO;
    public long orderCount = 0;

    public DailySalesRow(Date saleDate, BigDecimal grossTotal, BigDecimal paidTotal, long orderCount) {
        this.saleDate = saleDate;
        this.grossTotal = grossTotal;
        this.paidTotal = paidTotal;
        this.orderCount = orderCount;
    }
}
