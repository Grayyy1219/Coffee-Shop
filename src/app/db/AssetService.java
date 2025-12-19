package app.db;

import java.awt.Color;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class AssetService {

    public String getShopNameOrDefault() {
        byte[] data = getAssetBytes("shop_name");
        if (data == null || data.length == 0) return "Coffee Shop";
        String name = new String(data, StandardCharsets.UTF_8).trim();
        return name.isEmpty() ? "Coffee Shop" : name;
    }

    public ImageIcon getShopLogoOrNull(int targetSizePx) {
        byte[] data = getAssetBytes("shop_logo");
        if (data == null || data.length == 0) return null;

        try {
            Image img = ImageIO.read(new ByteArrayInputStream(data));
            if (img == null) return null;
            Image scaled = img.getScaledInstance(targetSizePx, targetSizePx, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }

    public Color getAccentColorOrDefault() {
        String hex = getAssetString("accent_color");
        if (hex == null || hex.isBlank()) return new Color(32, 85, 197);
        try {
            return Color.decode(hex.trim());
        } catch (NumberFormatException ex) {
            return new Color(32, 85, 197);
        }
    }

    public void saveShopName(String name) throws Exception {
        if (name == null) return;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return;
        upsertAsset("shop_name", trimmed.getBytes(StandardCharsets.UTF_8));
    }

    public void saveShopLogo(byte[] data) throws Exception {
        if (data == null || data.length == 0) return;
        upsertAsset("shop_logo", data);
    }

    public void saveAccentColor(Color color) throws Exception {
        if (color == null) return;
        String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        upsertAsset("accent_color", hex.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getAssetBytes(String key) {
        String sql = "SELECT asset_blob FROM system_assets WHERE asset_key = ?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("asset_blob");
                }
            }
        } catch (Exception ex) {
            // for student simplicity: just return null
        }
        return null;
    }

    private String getAssetString(String key) {
        byte[] data = getAssetBytes(key);
        if (data == null || data.length == 0) return null;
        return new String(data, StandardCharsets.UTF_8).trim();
    }

    private void upsertAsset(String key, byte[] data) throws Exception {
        String sql = "INSERT INTO system_assets (asset_key, asset_blob) VALUES (?, ?)\n" +
                "ON DUPLICATE KEY UPDATE asset_blob = VALUES(asset_blob)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setBytes(2, data);
            ps.executeUpdate();
        }
    }
}
