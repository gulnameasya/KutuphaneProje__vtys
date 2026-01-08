import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class KitapYonetimFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtAra, txtKitapAdi, txtYazar, txtYayinevi, txtBasimYili, txtToplamAdet;
    private JComboBox<String> cmbKategori;
    private JTextField txtID;

    public KitapYonetimFrame() {
        setTitle("Kitap Yönetimi");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("KİTAP YÖNETİMİ", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== ARAMA PANELİ =====
        JPanel panelAra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAra.setBackground(new Color(240, 255, 240));
        panelAra.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        panelAra.add(new JLabel("Arama (Adı/Yazar/Kategori/Yayınevi): "));
        txtAra = new JTextField(60);
        txtAra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtAra.setPreferredSize(new Dimension(700, 50));
        panelAra.add(txtAra);

        add(panelAra, BorderLayout.NORTH);

        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { kitapListele(txtAra.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { kitapListele(txtAra.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { kitapListele(txtAra.getText().trim()); }
        });

        // ===== TABLO =====
        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Basım Yılı", "Toplam Adet", "Mevcut Adet"
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
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) tabloSecim();
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(scrollTable, BorderLayout.CENTER);

        // ===== FORM PANELİ (SAĞDA) =====
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(new Color(240, 255, 240));
        panelForm.setBorder(BorderFactory.createTitledBorder("Kitap Bilgileri"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        txtID = new JTextField("-");
        txtID.setEditable(false);
        txtID.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtID.setHorizontalAlignment(JTextField.CENTER);
        txtID.setPreferredSize(new Dimension(100, 40));

        txtKitapAdi = new JTextField(30);
        txtYazar = new JTextField(30);
        txtYayinevi = new JTextField(30);
        txtBasimYili = new JTextField(15);
        txtToplamAdet = new JTextField(15);
        cmbKategori = new JComboBox<>();
        cmbKategori.setEditable(true);
        cmbKategori.setPreferredSize(new Dimension(300, 45));

        int y = 0;
        addFormRow(panelForm, gbc, y++, "ID:", txtID);
        addFormRow(panelForm, gbc, y++, "Kitap Adı *:", txtKitapAdi);
        addFormRow(panelForm, gbc, y++, "Yazar *:", txtYazar);
        addFormRow(panelForm, gbc, y++, "Kategori:", cmbKategori);
        addFormRow(panelForm, gbc, y++, "Yayınevi:", txtYayinevi);
        addFormRow(panelForm, gbc, y++, "Basım Yılı *:", txtBasimYili);
        addFormRow(panelForm, gbc, y++, "Toplam Adet *:", txtToplamAdet);

        // Butonlar
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        JButton btnEkle = createStyledButton("YENİ EKLE", new Color(0, 150, 0));
        JButton btnGuncelle = createStyledButton("GÜNCELLE", new Color(0, 100, 200));
        JButton btnSil = createStyledButton("SİL", new Color(200, 0, 0));
        JButton btnTemizle = createStyledButton("TEMİZLE", new Color(100, 100, 100));

        btnEkle.addActionListener(e -> kitapEkle());
        btnGuncelle.addActionListener(e -> kitapGuncelle());
        btnSil.addActionListener(e -> kitapSil());
        btnTemizle.addActionListener(e -> formTemizle());

        btnPanel.add(btnEkle);
        btnPanel.add(btnGuncelle);
        btnPanel.add(btnSil);
        btnPanel.add(btnTemizle);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        panelForm.add(btnPanel, gbc);

        add(panelForm, BorderLayout.EAST);

        // ===== ALT PANEL - GERİ DÖN =====
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

        add(panelAlt, BorderLayout.SOUTH);

        // Çarpıya basınca da ana menüye dön
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        kategorileriYukle();
        kitapListele("");
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int y, String label, JComponent comp) {
        g.gridx = 0; g.gridy = y;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(comp, g);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(200, 60));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void kategorileriYukle() {
        cmbKategori.removeAllItems();
        cmbKategori.addItem("Diğer");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT kategori FROM kitap WHERE kategori IS NOT NULL AND kategori != '' ORDER BY kategori")) {

            while (rs.next()) {
                String kat = rs.getString("kategori");
                if (!kat.equals("Diğer")) {
                    cmbKategori.addItem(kat);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Kategoriler yüklenemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kitapListele(String arama) {
        tableModel.setRowCount(0);

        String sql = "SELECT kitapid, kitapadi, yazar, kategori, yayinevi, basimyili, toplamadet, mevcutadet FROM kitap";
        if (!arama.isEmpty()) {
            sql += " WHERE LOWER(kitapadi) LIKE LOWER(?) OR LOWER(yazar) LIKE LOWER(?) OR LOWER(kategori) LIKE LOWER(?) OR LOWER(yayinevi) LIKE LOWER(?)";
        }
        sql += " ORDER BY kitapadi";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!arama.isEmpty()) {
                String pattern = "%" + arama.toLowerCase() + "%";
                for (int i = 1; i <= 4; i++) ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getLong("kitapid"),
                            rs.getString("kitapadi"),
                            rs.getString("yazar"),
                            rs.getString("kategori"),
                            rs.getString("yayinevi"),
                            rs.getInt("basimyili"),
                            rs.getInt("toplamadet"),
                            rs.getInt("mevcutadet")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Kitaplar listelenemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kitapEkle() {
        if (txtKitapAdi.getText().trim().isEmpty() || txtYazar.getText().trim().isEmpty() ||
            txtBasimYili.getText().trim().isEmpty() || txtToplamAdet.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Zorunlu alanları doldurun (*)", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int yil, adet;
        try {
            yil = Integer.parseInt(txtBasimYili.getText().trim());
            adet = Integer.parseInt(txtToplamAdet.getText().trim());
            if (yil < 1000 || yil > java.time.Year.now().getValue() + 5 || adet <= 0) {
                throw new Exception();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Geçersiz basım yılı veya adet!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kategori = cmbKategori.getSelectedItem() != null ? cmbKategori.getSelectedItem().toString().trim() : "Diğer";
        if (kategori.isEmpty()) kategori = "Diğer";

        String sql = "INSERT INTO kitap (kitapadi, yazar, kategori, yayinevi, basimyili, toplamadet, mevcutadet) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtKitapAdi.getText().trim());
            ps.setString(2, txtYazar.getText().trim());
            ps.setString(3, kategori);
            ps.setString(4, txtYayinevi.getText().trim());
            ps.setInt(5, yil);
            ps.setInt(6, adet);
            ps.setInt(7, adet);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kitap başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            kitapListele(txtAra.getText().trim());
            formTemizle();
            kategorileriYukle();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ekleme hatası:\n" + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kitapGuncelle() {
        if (txtID.getText().equals("-")) {
            JOptionPane.showMessageDialog(this, "Güncellemek için bir kitap seçin!", "Seçim Gerekli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtKitapAdi.getText().trim().isEmpty() || txtYazar.getText().trim().isEmpty() ||
            txtBasimYili.getText().trim().isEmpty() || txtToplamAdet.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Zorunlu alanları doldurun (*)", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int yil, adet;
        try {
            yil = Integer.parseInt(txtBasimYili.getText().trim());
            adet = Integer.parseInt(txtToplamAdet.getText().trim());
            if (yil < 1000 || yil > java.time.Year.now().getValue() + 5 || adet <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Geçersiz basım yılı veya adet!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int onay = JOptionPane.showConfirmDialog(this, "Kitap bilgilerini güncellemek istediğinize emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE kitap SET kitapadi=?, yazar=?, kategori=?, yayinevi=?, basimyili=?, toplamadet=? WHERE kitapid=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtKitapAdi.getText().trim());
            ps.setString(2, txtYazar.getText().trim());
            ps.setString(3, cmbKategori.getSelectedItem() != null ? cmbKategori.getSelectedItem().toString().trim() : "Diğer");
            ps.setString(4, txtYayinevi.getText().trim());
            ps.setInt(5, yil);
            ps.setInt(6, adet);
            ps.setLong(7, Long.parseLong(txtID.getText()));

            int etkilenen = ps.executeUpdate();
            if (etkilenen > 0) {
                JOptionPane.showMessageDialog(this, "Kitap başarıyla güncellendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                kitapListele(txtAra.getText().trim());
                kategorileriYukle();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Güncelleme hatası:\n" + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kitapSil() {
        if (txtID.getText().equals("-")) {
            JOptionPane.showMessageDialog(this, "Silmek için bir kitap seçin!", "Seçim Gerekli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int onay = JOptionPane.showConfirmDialog(this,
                "Bu kitabı silmek istediğinize emin misiniz?\n\nEğer ödünç verilmişse silinemez!",
                "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (onay != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM kitap WHERE kitapid = ?")) {

            ps.setLong(1, Long.parseLong(txtID.getText()));
            int etkilenen = ps.executeUpdate();

            if (etkilenen > 0) {
                JOptionPane.showMessageDialog(this, "Kitap başarıyla silindi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                kitapListele(txtAra.getText().trim());
                formTemizle();
            } else {
                JOptionPane.showMessageDialog(this, "Kitap silinemedi (ödünçte olabilir).", "Hata", JOptionPane.ERROR_MESSAGE);
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

        txtID.setText(tableModel.getValueAt(row, 0).toString());
        txtKitapAdi.setText(tableModel.getValueAt(row, 1).toString());
        txtYazar.setText(tableModel.getValueAt(row, 2).toString());
        String kat = tableModel.getValueAt(row, 3).toString();
        cmbKategori.setSelectedItem(kat);
        if (cmbKategori.getSelectedIndex() == -1) {
            cmbKategori.setSelectedItem("Diğer");
        }
        txtYayinevi.setText(tableModel.getValueAt(row, 4).toString());
        txtBasimYili.setText(tableModel.getValueAt(row, 5).toString());
        txtToplamAdet.setText(tableModel.getValueAt(row, 6).toString());
    }

    private void formTemizle() {
        txtID.setText("-");
        txtKitapAdi.setText("");
        txtYazar.setText("");
        txtYayinevi.setText("");
        txtBasimYili.setText("");
        txtToplamAdet.setText("");
        if (cmbKategori.getItemCount() > 0) cmbKategori.setSelectedIndex(0);
        table.clearSelection();
    }
}