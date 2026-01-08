import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class LoginFrame extends JFrame {

    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;
    private JCheckBox chkSifreyiGoster;

    public LoginFrame() {
        setTitle("Kütüphane Yönetim Sistemi - Giriş");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        setResizable(false);

        getContentPane().setBackground(new Color(240, 255, 240)); // Açık yeşil arka plan

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("KÜTÜPHANE YÖNETİM SİSTEMİ", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblBaslik.setForeground(new Color(0, 100, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblBaslik, gbc);

        JLabel lblAltBaslik = new JLabel("Yönetici ve Personel Girişi", SwingConstants.CENTER);
        lblAltBaslik.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lblAltBaslik.setForeground(new Color(0, 120, 0));
        gbc.gridy = 1;
        add(lblAltBaslik, gbc);

        // ===== KULLANICI ADI =====
        JLabel lblKullanici = new JLabel("Kullanıcı Adı:");
        lblKullanici.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        add(lblKullanici, gbc);

        txtKullaniciAdi = new JTextField(25);
        txtKullaniciAdi.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        txtKullaniciAdi.setPreferredSize(new Dimension(350, 50));
        gbc.gridx = 1;
        add(txtKullaniciAdi, gbc);

        // ===== ŞİFRE =====
        JLabel lblSifre = new JLabel("Şifre:");
        lblSifre.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridx = 0; gbc.gridy = 3;
        add(lblSifre, gbc);

        txtSifre = new JPasswordField(25);
        txtSifre.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        txtSifre.setPreferredSize(new Dimension(350, 50));
        gbc.gridx = 1;
        add(txtSifre, gbc);

        // Şifreyi göster
        chkSifreyiGoster = new JCheckBox("Şifreyi göster");
        chkSifreyiGoster.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chkSifreyiGoster.setBackground(new Color(240, 255, 240));
        chkSifreyiGoster.addActionListener(e -> {
            txtSifre.setEchoChar(chkSifreyiGoster.isSelected() ? (char) 0 : '•');
        });
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        add(chkSifreyiGoster, gbc);

        // ===== GİRİŞ BUTONU =====
        JButton btnGiris = new JButton("GİRİŞ YAP");
        btnGiris.setFont(new Font("Segoe UI", Font.BOLD, 28));
        btnGiris.setBackground(new Color(0, 140, 0));
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setPreferredSize(new Dimension(400, 70));
        btnGiris.setFocusPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnGiris, gbc);

        btnGiris.addActionListener(e -> girisYap());
        getRootPane().setDefaultButton(btnGiris);

        // Odak başlangıçta kullanıcı adı
        SwingUtilities.invokeLater(() -> txtKullaniciAdi.requestFocusInWindow());
    }

    private String hashSifre(String sifre) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sifre.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Şifre hashleme hatası", e);
        }
    }

    private void girisYap() {
        String kullaniciAdi = txtKullaniciAdi.getText().trim();
        String sifre = new String(txtSifre.getPassword());

        if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifreyi girin!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // GEÇİCİ: Düz metin şifre (test için)
        String sifreHash = sifre; // Teslim öncesi hashSifre(sifre) yap

        String sql = """
            SELECT r.RolAdi
            FROM KULLANICI k
            JOIN ROL r ON k.RolID = r.RolID
            WHERE LOWER(k.KullaniciAdi) = LOWER(?)
              AND k.SifreHash = ?
              AND k.AktifMi = TRUE
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kullaniciAdi);
            pstmt.setString(2, sifreHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("RolAdi");
                    JOptionPane.showMessageDialog(this, "Hoş geldiniz, " + rol + "!", "Giriş Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new AnaMenuFrame(rol).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Kullanıcı adı veya şifre hatalı!", "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
                    txtSifre.setText("");
                    chkSifreyiGoster.setSelected(false);
                    txtSifre.setEchoChar('•');
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Bağlantı hatası:\n" + ex.getMessage(), "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }
            new LoginFrame().setVisible(true);
        });
    }
}