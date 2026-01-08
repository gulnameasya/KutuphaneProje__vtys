import javax.swing.*;
import java.awt.*;

public class AnaMenuFrame extends JFrame {

    private final String rol;
    private static AnaMenuFrame instance; // Tek instance tutacağız

    public AnaMenuFrame(String rol) {
        this.rol = rol;
        instance = this; // Kendini statik olarak sakla

        String rolBuyuk = rol.substring(0, 1).toUpperCase() + rol.substring(1).toLowerCase();

        setTitle("Kütüphane Yönetim Sistemi - Hoş geldin " + rolBuyuk);
        setSize(1000, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("KÜTÜPHANE YÖNETİM SİSTEMİ", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(50, 0, 60, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== BUTON PANELİ =====
        JPanel panelButonlar = new JPanel(new GridBagLayout());
        panelButonlar.setBackground(new Color(240, 255, 240));
        panelButonlar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        JButton btnUye = createStyledButton("Üye Yönetimi", () -> openFrame(new UyeYonetimFrame()));
        JButton btnKitap = createStyledButton("Kitap Yönetimi", () -> openFrame(new KitapYonetimFrame()));
        JButton btnOdunc = createStyledButton("Ödünç Verme", () -> openFrame(new OduncVermeFrame()));
        JButton btnTeslim = createStyledButton("Kitap Teslim Alma", () -> openFrame(new KitapTeslimFrame()));
        JButton btnCeza = createStyledButton("Ceza Görüntüleme", () -> openFrame(new CezaGoruntulemeFrame()));
        JButton btnRapor = createStyledButton("Raporlar", () -> openFrame(new RaporlarFrame()));
        JButton btnDinamikArama = createStyledButton("Dinamik Kitap Arama", () -> openFrame(new DinamikAramaFrame()));

        JButton btnCikis = createStyledButton("ÇIKIŞ YAP", () -> {
            int secim = JOptionPane.showConfirmDialog(
                this,
                "Programdan tamamen çıkmak istediğinize emin misiniz?",
                "Çıkış Onayı",
                JOptionPane.YES_NO_OPTION
            );
            if (secim == JOptionPane.YES_OPTION) {
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window.isDisplayable()) {
                        window.dispose();
                    }
                }
                System.exit(0);
            }
        });
        btnCikis.setBackground(new Color(200, 0, 0));

        // Rol bazlı yetki
        if (!rol.equalsIgnoreCase("ADMIN")) {
            btnUye.setEnabled(false);
            btnCeza.setEnabled(false);
            btnRapor.setEnabled(false);
        }

        panelButonlar.add(btnUye, gbc);
        panelButonlar.add(btnKitap, gbc);
        panelButonlar.add(btnOdunc, gbc);
        panelButonlar.add(btnTeslim, gbc);
        panelButonlar.add(btnCeza, gbc);
        panelButonlar.add(btnRapor, gbc);
        panelButonlar.add(btnDinamikArama, gbc);
        panelButonlar.add(btnCikis, gbc);

        JScrollPane scrollPane = new JScrollPane(panelButonlar);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // ===== ALT BİLGİ =====
        JLabel lblAlt = new JLabel("Hoş geldin, " + rolBuyuk + " | Kütüphane Yönetim Sistemi v1.0", SwingConstants.CENTER);
        lblAlt.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblAlt.setForeground(new Color(0, 120, 0));
        lblAlt.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(lblAlt, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        btn.setBackground(new Color(0, 160, 0));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(700, 90));
        btn.setMaximumSize(new Dimension(700, 90));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        final Color normal = new Color(0, 160, 0);
        final Color hover = new Color(0, 190, 0);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hover);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normal);
            }
        });

        return btn;
    }

    private void openFrame(JFrame frame) {
        frame.setVisible(true);
        this.setVisible(false); // Ana menüyü gizle, ama yok etme!
    }

    // Diğer ekranlardan geri dönmek için statik metod
    public static void showMenu() {
        if (instance != null) {
            instance.setVisible(true);
            instance.toFront();
        }
    }
}