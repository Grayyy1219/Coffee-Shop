package app.db;

import app.model.DailySalesRow;
import app.model.DashboardSummary;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public DashboardSummary loadSummary() throws SQLException {
        DashboardSummary s = new DashboardSummary();

        // Today gross + paid (from orders)
        String sqlToday = """
            SELECT
              COALESCE(SUM(total), 0) AS gross_total,
              COALESCE(SUM(CASE WHEN paid = 1 THEN total ELSE 0 END), 0) AS paid_total
            FROM orders
            WHERE DATE(created_at) = CURDATE()
        """;

        // Queue count (treat PENDING and IN_PROGRESS as queue)
        String sqlQueue = """
            SELECT COUNT(*) AS c
            FROM orders
            WHERE status IN ('PENDING','IN_PROGRESS')
        """;

        // Completed today (status COMPLETED)
        String sqlCompleted = """
            SELECT COUNT(*) AS c
            FROM orders
            WHERE status = 'COMPLETED' AND DATE(created_at) = CURDATE()
        """;

        // Total users
        String sqlUsers = "SELECT COUNT(*) AS c FROM users";

        try (Connection con = DB.getConnection()) {
            // Today totals
            try (PreparedStatement ps = con.prepareStatement(sqlToday);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s.todayGross = rs.getBigDecimal("gross_total");
                    s.todayPaid  = rs.getBigDecimal("paid_total");
                }
            }

            // Queue
            try (PreparedStatement ps = con.prepareStatement(sqlQueue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s.ordersInQueue = rs.getInt("c");
            }

            // Completed today
            try (PreparedStatement ps = con.prepareStatement(sqlCompleted);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s.completedToday = rs.getInt("c");
            }

            // Users
            try (PreparedStatement ps = con.prepareStatement(sqlUsers);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s.totalUsers = rs.getInt("c");
            }
        }

        // null-safe
        if (s.todayGross == null) s.todayGross = BigDecimal.ZERO;
        if (s.todayPaid == null) s.todayPaid = BigDecimal.ZERO;

        return s;
    }

    /** Uses your DB view v_daily_sales if present. */
    public List<DailySalesRow> loadRecentDailySales(int days) throws SQLException {
        List<DailySalesRow> out = new ArrayList<>();

        String sql = """
            SELECT sale_date, gross_total, paid_total, order_count
            FROM v_daily_sales
            WHERE sale_date >= (CURDATE() - INTERVAL ? DAY)
            ORDER BY sale_date DESC
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Math.max(days, 1));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("sale_date");
                    BigDecimal gross = rs.getBigDecimal("gross_total");
                    BigDecimal paid = rs.getBigDecimal("paid_total");
                    long count = rs.getLong("order_count");

                    out.add(new DailySalesRow(
                            d,
                            gross == null ? BigDecimal.ZERO : gross,
                            paid == null ? BigDecimal.ZERO : paid,
                            count
                    ));
                }
            }
        }
        return out;
        
    }
    
}
