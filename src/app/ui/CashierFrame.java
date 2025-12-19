package app.ui;

import app.db.AssetService;

import javax.swing.*;
import java.awt.*;

public class CashierFrame extends JFrame {

    public CashierFrame(String username) {
        setTitle("Cashier");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(buildUI(username));
    }

    private JComponent buildUI(String username) {
        AssetService assets = new AssetService();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        root.add(buildTopBar(username, assets), BorderLayout.NORTH);
        root.add(new CashierPanel(false, username, assets.getShopNameOrDefault()), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildTopBar(String username, AssetService assets) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Cashier");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(30, 41, 59));

        JLabel user = new JLabel("Logged in as: " + username);
        user.setFont(new Font("SansSerif", Font.PLAIN, 12));
        user.setForeground(new Color(100, 116, 139));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBackground(assets.getAccentColorOrDefault());
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnLogout.addActionListener(e -> doLogout());

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(user);
        right.add(Box.createHorizontalStrut(12));
        right.add(btnLogout);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Log out now?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        new LoginFrame().setVisible(true);
        dispose();
    }
}
