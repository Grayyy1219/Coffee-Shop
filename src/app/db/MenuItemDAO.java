package app.db;

import app.model.MenuItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> findAll() throws Exception {
        String sql = "SELECT code, name, category, price FROM menu_items ORDER BY name ASC";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<MenuItem> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new MenuItem(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price")
                ));
            }
            return out;
        }
    }
}
