package app.db;

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
}
