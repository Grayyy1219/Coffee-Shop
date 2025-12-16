package app.ui;

import app.db.AssetService;
import app.db.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginFrame extends JFrame {

    private final JLabel lblLogo = new JLabel();
    private final JLabel lblShopName = new JLabel("Coffee Shop");
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JCheckBox chkShow = new JCheckBox("Show password");
    private final JButton btnLogin = new JButton("Sign in");
    private final JLabel lblStatus = new JLabel(" ");

    private final AssetService assetService = new AssetService();
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        setContentPane(buildUI());
        applyBrandingFromDB();
        wireEvents();
    }

    private JComponent buildUI() {
        // Root: full-screen soft background, center the card.
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        ModernCard card = new ModernCard(22);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(520, 560));

        // Header
        HeaderPanel header = new HeaderPanel(22);
        header.setLayout(new GridBagLayout());
        header.setBorder(new EmptyBorder(22, 22, 18, 22));

        GridBagConstraints h = new GridBagConstraints();
        h.gridx = 0;
        h.weightx = 1;
        h.fill = GridBagConstraints.HORIZONTAL;

        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setPreferredSize(new Dimension(84, 84));

        lblShopName.setHorizontalAlignment(SwingConstants.CENTER);
        lblShopName.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblShopName.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Staff Login", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(235, 238, 242));

        h.gridy = 0;
        header.add(lblLogo, h);
        h.gridy = 1;
        h.insets = new Insets(10, 0, 0, 0);
        header.add(lblShopName, h);
        h.gridy = 2;
        h.insets = new Insets(4, 0, 0, 0);
        header.add(lblSubtitle, h);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 26, 22, 26));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblWelcome.setForeground(new Color(33, 37, 41));

        JLabel lblHint = new JLabel("Please sign in to continue.");
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblHint.setForeground(new Color(108, 117, 125));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 2, 0);
        body.add(lblWelcome, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 16, 0);
        body.add(lblHint, c);

        // Username row
        c.gridy = 2;
        c.insets = new Insets(0, 0, 12, 0);
        body.add(labeledField("Username", txtUsername, "Enter your username"), c);

        // Password row
        c.gridy = 3;
        c.insets = new Insets(0, 0, 8, 0);
        body.add(labeledField("Password", txtPassword, "Enter your password"), c);

        // Show password
        chkShow.setOpaque(false);
        chkShow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkShow.setForeground(new Color(73, 80, 87));

        c.gridy = 4;
        c.insets = new Insets(0, 0, 16, 0);
        body.add(chkShow, c);

        // Button row (fills width, hugs height)
        stylePrimaryButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(10, 44)); // height; width ignored by layout

        c.gridy = 5;
        c.insets = new Insets(0, 0, 12, 0);
        body.add(btnLogin, c);

        // Status
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(220, 53, 69));

        c.gridy = 6;
        c.insets = new Insets(0, 0, 0, 0);
        body.add(lblStatus, c);

        // Push everything to top (so it doesn't float weirdly when card height grows)
        c.gridy = 7;
        c.weighty = 1;
        body.add(Box.createVerticalGlue(), c);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0;
        r.gridy = 0;
        r.insets = new Insets(24, 24, 24, 24);
        root.add(card, r);

        return root;
    }

    private JPanel labeledField(String label, JComponent field, String tooltip) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(73, 80, 87));

        field.setToolTipText(tooltip);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(10, 44)); // height; width handled by layout
        setFieldBorder(field, false);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { setFieldBorder(field, true); }
            @Override public void focusLost(FocusEvent e)  { setFieldBorder(field, false); }
        });

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridy = 0;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(l, g);

        g.gridy = 1;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(field, g);

        return p;
    }

    private void setFieldBorder(JComponent field, boolean focused) {
        Color line = focused ? new Color(32, 85, 197) : new Color(222, 226, 230);
        int thickness = focused ? 2 : 1;
        int pad = focused ? 9 : 10;

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, thickness),
                BorderFactory.createEmptyBorder(pad, 12, pad, 12)
        ));
    }

    private void stylePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(32, 85, 197));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14)); // hug content visually
    }

    private void applyBrandingFromDB() {
        lblShopName.setText(assetService.getShopNameOrDefault());

        ImageIcon logo = assetService.getShopLogoOrNull(84);
        if (logo != null) {
            lblLogo.setIcon(logo);
        } else {
            lblLogo.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        }
        lblLogo.setText("");
    }

    private void wireEvents() {
        // If you previously changed echo char, you can store it; this is fine for most UIs.
        chkShow.addActionListener(e -> txtPassword.setEchoChar(chkShow.isSelected() ? (char) 0 : '•'));

        btnLogin.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        lblStatus.setText(" ");

        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        var result = authService.authenticate(user, pass);
        if (!result.ok) {
            lblStatus.setText(result.message);
            return;
        }
if ("OWNER".equalsIgnoreCase(result.role)) {
    new OwnerFrame(user).setVisible(true);
    dispose();
    return;
}

// Later:
// else if CASHIER -> open CashierFrame
// else if BARISTA -> open BaristaFrame

        JOptionPane.showMessageDialog(this,
                "Welcome, " + user + " (" + result.role + ")",
                "Login Success",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: open role frame then dispose
        // new OwnerFrame().setVisible(true); dispose();
        
    }

    // ---------- UI helper panels ----------

    /** Rounded card with a soft shadow (correct painting, no setBounds hacks). */
    static class ModernCard extends JPanel {
        private final int arc;

        ModernCard(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Shadow
            g2.setColor(new Color(0, 0, 0, 22));
            g2.fillRoundRect(10, 10, w - 20, h - 20, arc, arc);

            // Card
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 20, h - 20, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            // Keep children inside the WHITE card area (since we draw it smaller)
            return new Insets(0, 0, 20, 20);
        }
    }

    /** Gradient header bar that matches the card arc. */
    static class HeaderPanel extends JPanel {
        private final int arc;

        HeaderPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(32, 85, 197),
                    w, h, new Color(18, 54, 120)
            );
            g2.setPaint(gp);

            // draw slightly taller so the bottom looks smooth
            g2.fillRoundRect(0, 0, w, h + 24, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
