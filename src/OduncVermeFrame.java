import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class OduncVermeFrame extends JFrame {

    private JComboBox<ComboItem> cmbUye;
    private JComboBox<ComboItem> cmbKitap;
    private JLabel lblUyeBilgi, lblKitapBilgi;
    private DefaultComboBoxModel<ComboItem> modelUye, modelKitap;
    private JTextField txtUyeAra, txtKitapAra;

    private final long islemYapanID = 1L; 

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public OduncVermeFrame() {
        setTitle("Ödünç Verme İşlemi");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("ÖDÜNÇ VERME", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== ANA PANEL =====
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 255, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 60, 40, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Üye Ara:"), gbc);

        txtUyeAra = new JTextField(35);
        txtUyeAra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtUyeAra.setPreferredSize(new Dimension(500, 50));
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(txtUyeAra, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Üye Seç:"), gbc);

        modelUye = new DefaultComboBoxModel<>();
        cmbUye = new JComboBox<>(modelUye);
        cmbUye.setPreferredSize(new Dimension(600, 55));
        cmbUye.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(cmbUye, gbc);

        lblUyeBilgi = new JLabel(" ");
        lblUyeBilgi.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblUyeBilgi.setForeground(new Color(0, 80, 150));
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        panel.add(lblUyeBilgi, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Kitap Ara:"), gbc);

        txtKitapAra = new JTextField(35);
        txtKitapAra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtKitapAra.setPreferredSize(new Dimension(500, 50));
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(txtKitapAra, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Kitap Seç:"), gbc);

        modelKitap = new DefaultComboBoxModel<>();
        cmbKitap = new JComboBox<>(modelKitap);
        cmbKitap.setPreferredSize(new Dimension(600, 55));
        cmbKitap.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(cmbKitap, gbc);

        lblKitapBilgi = new JLabel(" ");
        lblKitapBilgi.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblKitapBilgi.setForeground(new Color(0, 80, 150));
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3;
        panel.add(lblKitapBilgi, gbc);

        JButton btnOduncVer = new JButton("ÖDÜNÇ VER");
        btnOduncVer.setFont(new Font("Segoe UI", Font.BOLD, 32));
        btnOduncVer.setBackground(new Color(0, 140, 0));
        btnOduncVer.setForeground(Color.WHITE);
        btnOduncVer.setPreferredSize(new Dimension(500, 90));
        btnOduncVer.setFocusPainted(false);
        btnOduncVer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnOduncVer, gbc);

        add(panel, BorderLayout.CENTER);

        // ===== ALT PANEL - GERİ DÖN =====
        JPanel panelAlt = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAlt.setBackground(new Color(240, 255, 240));
        panelAlt.setBorder(BorderFactory.createEmptyBorder(10, 60, 30, 60));

        JButton btnGeri = new JButton("← Ana Menüye Dön");
        btnGeri.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnGeri.setBackground(new Color(100, 100, 100));
        btnGeri.setForeground(Color.WHITE);
        btnGeri.setPreferredSize(new Dimension(250, 50));
        btnGeri.setFocusPainted(false);
        btnGeri.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGeri.addActionListener(e -> {
            dispose();
            AnaMenuFrame.showMenu();
        });
        panelAlt.add(btnGeri);

        add(panelAlt, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        // ===== EVENTLER =====
        btnOduncVer.addActionListener(e -> oduncVer());

        cmbUye.addActionListener(e -> uyeBilgiGoster());
        cmbKitap.addActionListener(e -> kitapBilgiGoster());

        txtUyeAra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { uyeFiltrele(); }
            public void removeUpdate(DocumentEvent e) { uyeFiltrele(); }
            public void changedUpdate(DocumentEvent e) { uyeFiltrele(); }
        });

        txtKitapAra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { kitapFiltrele(); }
            public void removeUpdate(DocumentEvent e) { kitapFiltrele(); }
            public void changedUpdate(DocumentEvent e) { kitapFiltrele(); }
        });

        uyeDoldur("");
        kitapDoldur("");
    }


    private void uyeFiltrele() {
        String arama = txtUyeAra.getText().trim().toLowerCase();
        uyeDoldur(arama);
    }

    private void kitapFiltrele() {
        String arama = txtKitapAra.getText().trim().toLowerCase();
        kitapDoldur(arama);
    }

    private void uyeDoldur(String arama) {
        modelUye.removeAllElements();

        String sql = "SELECT uyeid, adsoyad, toplamborc FROM uye WHERE LOWER(adsoyad) LIKE LOWER(?) ORDER BY adsoyad";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + arama + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("adsoyad");
                    BigDecimal borc = rs.getBigDecimal("toplamborc");
                    if (borc != null && borc.compareTo(BigDecimal.ZERO) > 0) {
                        text += " (Borç: " + borc + " TL)";
                    }
                    modelUye.addElement(new ComboItem(rs.getLong("uyeid"), text));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Üyeler yüklenemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kitapDoldur(String arama) {
        modelKitap.removeAllElements();

        String sql = """
            SELECT kitapid, kitapadi, yazar, mevcutadet, toplamadet
            FROM kitap
            WHERE mevcutadet > 0
              AND (LOWER(kitapadi) LIKE LOWER(?) OR LOWER(yazar) LIKE LOWER(?))
            ORDER BY kitapadi
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + arama + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("kitapadi") + " - " + rs.getString("yazar") +
                                  " (Stok: " + rs.getInt("mevcutadet") + "/" + rs.getInt("toplamadet") + ")";
                    modelKitap.addElement(new ComboItem(rs.getLong("kitapid"), text));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Kitaplar yüklenemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uyeBilgiGoster() {
        ComboItem uye = (ComboItem) cmbUye.getSelectedItem();
        if (uye == null) {
            lblUyeBilgi.setText(" ");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT telefon, email, toplamborc FROM uye WHERE uyeid=?")) {

            ps.setLong(1, uye.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal borc = rs.getBigDecimal("toplamborc");
                    String borcDurum = borc.compareTo(BigDecimal.ZERO) > 0
                            ? "<font color='red'><b>BORÇLU: " + borc + " TL</b></font>"
                            : "<font color='green'><b>Borç yok</b></font>";

                    lblUyeBilgi.setText("<html><b>Telefon:</b> " + rs.getString("telefon") +
                            " | <b>Email:</b> " + rs.getString("email") +
                            " | <b>Durum:</b> " + borcDurum + "</html>");
                }
            }
        } catch (SQLException e) {
            lblUyeBilgi.setText("<font color='red'>Bilgi yüklenemedi</font>");
        }
    }

    private void kitapBilgiGoster() {
        ComboItem kitap = (ComboItem) cmbKitap.getSelectedItem();
        if (kitap == null) {
            lblKitapBilgi.setText(" ");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT kategori, yayinevi, mevcutadet, toplamadet FROM kitap WHERE kitapid=?")) {

            ps.setLong(1, kitap.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblKitapBilgi.setText("<html><b>Kategori:</b> " + rs.getString("kategori") +
                            " | <b>Yayınevi:</b> " + rs.getString("yayinevi") +
                            " | <b>Stok:</b> <b>" + rs.getInt("mevcutadet") + "/" + rs.getInt("toplamadet") + "</b></html>");
                }
            }
        } catch (SQLException e) {
            lblKitapBilgi.setText("<font color='red'>Bilgi yüklenemedi</font>");
        }
    }

    private void oduncVer() {
        ComboItem uye = (ComboItem) cmbUye.getSelectedItem();
        ComboItem kitap = (ComboItem) cmbKitap.getSelectedItem();

        if (uye == null || kitap == null) {
            JOptionPane.showMessageDialog(this, "Lütfen hem üye hem kitap seçin!", "Eksik Seçim", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT toplamborc FROM uye WHERE uyeid=?")) {

            ps.setLong(1, uye.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal borc = rs.getBigDecimal("toplamborc");
                    if (borc.compareTo(BigDecimal.ZERO) > 0) {
                        int secim = JOptionPane.showConfirmDialog(this,
                                "Üyenin " + borc + " TL borcu var!\nYine de ödünç vermek istiyor musunuz?",
                                "Borç Uyarısı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (secim != JOptionPane.YES_OPTION) return;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Borç kontrolü yapılamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mesaj = "<html><b>Ödünç verme onayı:</b><br><br>" +
                       "Üye: <b>" + uye.getText() + "</b><br>" +
                       "Kitap: <b>" + kitap.getText().split(" - ")[0] + "</b><br>" +
                       "Teslim Tarihi: <b>" + DATE_FORMAT.format(new Date()) + "</b><br>" +
                       "İade Tarihi: <b>15 gün sonra</b></html>";

        int onay = JOptionPane.showConfirmDialog(this, mesaj, "Onaylıyor musunuz?", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall("CALL sp_yenioduncver(?, ?, ?)")) {
                cs.setLong(1, uye.getId());
                cs.setLong(2, kitap.getId());
                cs.setLong(3, islemYapanID);

                cs.executeUpdate();
                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "<html><b>Ödünç verme başarılı!</b><br><br>Kitap başarıyla ödünç verildi.<br>İade süresi: <b>15 gün</b></html>",
                        "Başarılı", JOptionPane.INFORMATION_MESSAGE);

                kitapDoldur(txtKitapAra.getText().trim());
                kitapBilgiGoster();

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "Ödünç verilemedi:\n" + ex.getMessage(),
                        "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

}
