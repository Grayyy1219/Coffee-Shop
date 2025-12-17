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
        root.add(new CashierPanel(false, username, assets.getShopNameOrDefault()), BorderLayout.CENTER);
        return root;
    }
}
