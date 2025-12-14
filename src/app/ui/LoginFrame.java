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
        // Root with soft background
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        ModernCard card = new ModernCard();
        card.setPreferredSize(new Dimension(570, 720));
        card.setLayout(new BorderLayout());

        // Header (gradient) + logo + shop name
        HeaderPanel header = new HeaderPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(22, 22, 18, 22));

        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setPreferredSize(new Dimension(84, 84));
        lblLogo.setMinimumSize(new Dimension(84, 84));
        lblLogo.setMaximumSize(new Dimension(84, 84));

        lblShopName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblShopName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblShopName.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Staff Login");
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(235, 238, 242));

        header.add(lblLogo);
        header.add(Box.createVerticalStrut(10));
        header.add(lblShopName);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSubtitle);

        // Body (form)
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblWelcome.setForeground(new Color(33, 37, 41));
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint = new JLabel("Please sign in to continue.");
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblHint.setForeground(new Color(108, 117, 125));
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(lblWelcome);
        body.add(Box.createVerticalStrut(2));
        body.add(lblHint);
        body.add(Box.createVerticalStrut(16));

        // Fields
    
        body.add(fieldBlock("Username", txtUsername, "Enter your username"));
        body.add(Box.createVerticalStrut(12));
        body.add(fieldBlock("Password", txtPassword, "Enter your password"));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 440));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 440));
        body.add(Box.createVerticalStrut(10));

        // Show password (modern style)
        chkShow.setOpaque(false);
        chkShow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkShow.setForeground(new Color(73, 80, 87));
        chkShow.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(chkShow);

        body.add(Box.createVerticalStrut(16));

        // Button (primary)
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setBackground(new Color(32, 85, 197));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Make it fill width
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.add(btnLogin, BorderLayout.CENTER);

        body.add(btnWrap);


        body.add(Box.createVerticalStrut(12));

        // Status label
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(220, 53, 69));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblStatus);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        root.add(card);
        return root;
    }

    private JPanel fieldBlock(String label, JComponent field, String tooltip) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(73, 80, 87));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setToolTipText(tooltip);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Modern field border
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Focus glow (simple)
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(32, 85, 197), 2),
                        BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
            }
        });

        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private void applyBrandingFromDB() {
        // Shop Name (fallback)
        lblShopName.setText(assetService.getShopNameOrDefault());

        // Logo (fallback to a simple UI icon)
        ImageIcon logo = assetService.getShopLogoOrNull(84);
        if (logo != null) {
            lblLogo.setIcon(logo);
        } else {
            lblLogo.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        }
        lblLogo.setText("");
    }

    private void wireEvents() {
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

        JOptionPane.showMessageDialog(this,
                "Welcome, " + user + " (" + result.role + ")",
                "Login Success",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: open role frame then dispose
        // new OwnerFrame().setVisible(true); dispose();
    }

    // ---------- Modern UI helper panels ----------

    /** Rounded card with a soft shadow. */
    static class ModernCard extends JPanel {
        ModernCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(14, 14, 14, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 22;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 22));
            g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, arc, arc);

            // Card
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, arc, arc);

            g2.dispose();

            // Layout children inside the “card area”
            setBounds(getX(), getY(), getWidth(), getHeight());
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            return new Insets(0, 0, 16, 16); // keep children inside white area
        }
    }

    /** Gradient header bar. */
    static class HeaderPanel extends JPanel {
        HeaderPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 22;
            int w = getWidth();
            int h = getHeight();

            // Gradient (top-left to bottom-right)
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(32, 85, 197),
                    w, h, new Color(18, 54, 120)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h + 20, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
