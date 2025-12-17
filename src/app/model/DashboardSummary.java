package app.model;

import java.math.BigDecimal;

public class DashboardSummary {
    public BigDecimal todayGross = BigDecimal.ZERO;
    public BigDecimal todayPaid = BigDecimal.ZERO;

    public int ordersInQueue = 0;      // PENDING / IN_PROGRESS
    public int completedToday = 0;     // COMPLETED today
    public int totalUsers = 0;         // count(users)
}
