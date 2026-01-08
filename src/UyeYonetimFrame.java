import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class UyeYonetimFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtAra, txtAdSoyad, txtTelefon, txtEmail;
    private JLabel lblID;

    public UyeYonetimFrame() {
        setTitle("Üye Yönetimi");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("ÜYE YÖNETİMİ", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== ARAMA PANELİ =====
        JPanel panelAra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAra.setBackground(new Color(240, 255, 240));
        panelAra.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        panelAra.add(new JLabel("Arama (Ad / Telefon / Email): "));
        txtAra = new JTextField(60);
        txtAra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtAra.setPreferredSize(new Dimension(700, 50));
        panelAra.add(txtAra);

        JButton btnAra = new JButton("ARA");
        btnAra.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnAra.setBackground(new Color(0, 140, 0));
        btnAra.setForeground(Color.WHITE);
        btnAra.setPreferredSize(new Dimension(150, 50));
        btnAra.setFocusPainted(false);
        panelAra.add(btnAra);

        add(panelAra, BorderLayout.NORTH);

        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { uyeListele(txtAra.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { uyeListele(txtAra.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { uyeListele(txtAra.getText().trim()); }
        });
        btnAra.addActionListener(e -> uyeListele(txtAra.getText().trim()));

        // ===== TABLO =====
        tableModel = new DefaultTableModel(new Object[]{
                "UyeID", "Ad Soyad", "Telefon", "Email", "Toplam Borç (TL)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));
        table.getTableHeader().setBackground(new Color(0, 120, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(40);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) tabloSecim();
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(scrollTable, BorderLayout.CENTER);

        // ===== FORM PANELİ (ALTTA) =====
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(new Color(240, 255, 240));
        panelForm.setBorder(BorderFactory.createTitledBorder("Üye Bilgileri"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        lblID = new JLabel("-");
        lblID.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblID.setForeground(new Color(0, 100, 0));

        txtAdSoyad = new JTextField(30);
        txtTelefon = new JTextField(30);
        txtEmail = new JTextField(30);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panelForm.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; panelForm.add(lblID, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelForm.add(new JLabel("Ad Soyad *:"), gbc);
        gbc.gridx = 1; panelForm.add(txtAdSoyad, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelForm.add(new JLabel("Telefon *:"), gbc);
        gbc.gridx = 1; panelForm.add(txtTelefon, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelForm.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1; panelForm.add(txtEmail, gbc);

        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 20, 0));
        JButton btnYeni = createStyledButton("YENİ", new Color(0, 150, 0));
        JButton btnKaydet = createStyledButton("KAYDET", new Color(0, 140, 0));
        JButton btnGuncelle = createStyledButton("GÜNCELLE", new Color(0, 100, 200));
        JButton btnSil = createStyledButton("SİL", new Color(200, 0, 0));
        JButton btnTemizle = createStyledButton("TEMİZLE", new Color(100, 100, 100));

        btnYeni.addActionListener(e -> formTemizle());
        btnKaydet.addActionListener(e -> uyeEkle());
        btnGuncelle.addActionListener(e -> uyeGuncelle());
        btnSil.addActionListener(e -> uyeSil());
        btnTemizle.addActionListener(e -> formTemizle());

        btnPanel.add(btnYeni);
        btnPanel.add(btnKaydet);
        btnPanel.add(btnGuncelle);
        btnPanel.add(btnSil);
        btnPanel.add(btnTemizle);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelForm.add(btnPanel, gbc);

        add(panelForm, BorderLayout.SOUTH);

        // ===== GERİ DÖN BUTONU VE ÇARPI DAVRANIŞI =====
        JPanel panelAlt = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAlt.setBackground(new Color(240, 255, 240));
        panelAlt.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

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

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(240, 255, 240));
        wrapper.add(panelForm, BorderLayout.CENTER);
        wrapper.add(panelAlt, BorderLayout.SOUTH);

        add(wrapper, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        uyeListele("");
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(180, 60));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void uyeListele(String arama) {
        tableModel.setRowCount(0);

        String sql = """
            SELECT UyeID, AdSoyad, Telefon, Email, ToplamBorc
            FROM UYE
            WHERE (? = '' OR LOWER(AdSoyad) LIKE LOWER(?) OR LOWER(Telefon) LIKE LOWER(?) OR LOWER(Email) LIKE LOWER(?))
            ORDER BY UyeID
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + arama.toLowerCase() + "%";
            ps.setString(1, arama);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getLong("UyeID"),
                            rs.getString("AdSoyad"),
                            rs.getString("Telefon"),
                            rs.getString("Email"),
                            rs.getBigDecimal("ToplamBorc") + " TL"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Listeleme hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void formTemizle() {
        lblID.setText("-");
        txtAdSoyad.setText("");
        txtTelefon.setText("");
        txtEmail.setText("");
        table.clearSelection();
    }

    private boolean alanlarBosMu() {
        return txtAdSoyad.getText().trim().isEmpty() ||
               txtTelefon.getText().trim().isEmpty() ||
               txtEmail.getText().trim().isEmpty();
    }

    private boolean emailGecerliMi(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean telefonGecerliMi(String tel) {
        return tel.matches("\\d{10,11}");
    }

    private void uyeEkle() {
        if (alanlarBosMu()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurun!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!telefonGecerliMi(txtTelefon.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Telefon numarası 10-11 rakam olmalı!", "Geçersiz Telefon", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!emailGecerliMi(txtEmail.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Geçerli bir email girin!", "Geçersiz Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO UYE (AdSoyad, Telefon, Email, ToplamBorc) VALUES (?, ?, ?, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtAdSoyad.getText().trim());
            ps.setString(2, txtTelefon.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Üye başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            formTemizle();
            uyeListele(txtAra.getText().trim());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ekleme hatası:\n" + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uyeGuncelle() {
        if (lblID.getText().equals("-")) {
            JOptionPane.showMessageDialog(this, "Güncellemek için bir üye seçin!", "Seçim Gerekli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (alanlarBosMu()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurun!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!telefonGecerliMi(txtTelefon.getText().trim()) || !emailGecerliMi(txtEmail.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Telefon veya email geçersiz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int onay = JOptionPane.showConfirmDialog(this, "Üye bilgilerini güncellemek istediğinize emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE UYE SET AdSoyad=?, Telefon=?, Email=? WHERE UyeID=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtAdSoyad.getText().trim());
            ps.setString(2, txtTelefon.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.setLong(4, Long.parseLong(lblID.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Üye başarıyla güncellendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            uyeListele(txtAra.getText().trim());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası:\n" + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uyeSil() {
        if (lblID.getText().equals("-")) {
            JOptionPane.showMessageDialog(this, "Silmek için bir üye seçin!", "Seçim Gerekli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int onay = JOptionPane.showConfirmDialog(this,
                "Bu üyeyi silmek istediğinize emin misiniz?\n\nDikkat: Aktif ödünç veya borç varsa silinemeyebilir!",
                "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (onay != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM UYE WHERE UyeID=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(lblID.getText()));
            int etkilenen = ps.executeUpdate();

            if (etkilenen > 0) {
                JOptionPane.showMessageDialog(this, "Üye başarıyla silindi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                formTemizle();
                uyeListele(txtAra.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "Üye silinemedi (ödünç veya borç kaydı olabilir).", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Silme hatası:\n" + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tabloSecim() {
        int row = table.getSelectedRow();
        if (row == -1) {
            formTemizle();
            return;
        }

        lblID.setText(tableModel.getValueAt(row, 0).toString());
        txtAdSoyad.setText(tableModel.getValueAt(row, 1).toString());
        txtTelefon.setText(tableModel.getValueAt(row, 2).toString());
        txtEmail.setText(tableModel.getValueAt(row, 3).toString());
    }

}
