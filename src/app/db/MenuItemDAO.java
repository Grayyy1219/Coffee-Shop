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

    public MenuItem findByCode(String code) throws Exception {
        String sql = "SELECT code, name, category, price FROM menu_items WHERE code = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new MenuItem(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price")
                );
            }
        }
    }

    public int insert(MenuItem item) throws Exception {
        String sql = "INSERT INTO menu_items (code, name, category, price) VALUES (?, ?, ?, ?)";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getCode());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setBigDecimal(4, item.getPrice());
            return ps.executeUpdate();
        }
    }

    public int update(MenuItem item) throws Exception {
        String sql = "UPDATE menu_items SET name = ?, category = ?, price = ? WHERE code = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getCode());
            return ps.executeUpdate();
        }
    }

    public int deleteByCode(String code) throws Exception {
        String sql = "DELETE FROM menu_items WHERE code = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate();
        }
    }
}
