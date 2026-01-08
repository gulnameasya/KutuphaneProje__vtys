import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class DinamikAramaFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtKitapAdi, txtYazar, txtBasimYilMin, txtBasimYilMax;
    private JComboBox<String> cmbKategori;
    private JCheckBox chkSadeceMevcut;
    private JComboBox<String> cmbSort;

    public DinamikAramaFrame() {
        setTitle("Dinamik Kitap Arama");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("DİNAMİK KİTAP ARAMA", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== FİLTRE PANELİ (ÜSTTE) =====
        JPanel panelFiltre = new JPanel(new GridBagLayout());
        panelFiltre.setBackground(new Color(240, 255, 240));
        panelFiltre.setBorder(BorderFactory.createTitledBorder("Arama Kriterleri"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        txtKitapAdi = new JTextField(25);
        txtYazar = new JTextField(25);
        cmbKategori = new JComboBox<>(new String[]{"Tümü", "Bilgisayar", "Edebiyat", "Tarih", "Bilim", "Diğer"});
        txtBasimYilMin = new JTextField(10);
        txtBasimYilMax = new JTextField(10);
        chkSadeceMevcut = new JCheckBox("Sadece Mevcut Olanlar", true);
        cmbSort = new JComboBox<>(new String[]{
                "Kitap Adı (A-Z)",
                "Kitap Adı (Z-A)",
                "Yazar (A-Z)",
                "Basım Yılı (Yeni → Eski)",
                "Basım Yılı (Eski → Yeni)"
        });
        cmbSort.setSelectedIndex(0);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panelFiltre.add(new JLabel("Kitap Adı:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panelFiltre.add(txtKitapAdi, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 1; panelFiltre.add(new JLabel("Yazar:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panelFiltre.add(txtYazar, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelFiltre.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1; panelFiltre.add(cmbKategori, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelFiltre.add(new JLabel("Basım Yılı Aralığı:"), gbc);
        gbc.gridx = 1; panelFiltre.add(txtBasimYilMin, gbc);
        gbc.gridx = 2; panelFiltre.add(new JLabel(" - "), gbc);
        gbc.gridx = 3; panelFiltre.add(txtBasimYilMax, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2; panelFiltre.add(chkSadeceMevcut, gbc);

        gbc.gridx = 0; gbc.gridy = ++y; panelFiltre.add(new JLabel("Sıralama:"), gbc);
        gbc.gridx = 1; panelFiltre.add(cmbSort, gbc);

        JButton btnAra = new JButton("KİTAPLARI ARA");
        btnAra.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnAra.setBackground(new Color(0, 140, 0));
        btnAra.setForeground(Color.WHITE);
        btnAra.setPreferredSize(new Dimension(300, 60));
        btnAra.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panelFiltre.add(btnAra, gbc);

        add(panelFiltre, BorderLayout.NORTH);

        // ===== TABLO =====
        tableModel = new DefaultTableModel(new Object[]{
                "Kitap ID", "Kitap Adı", "Yazar", "Kategori",
                "Yayınevi", "Basım Yılı", "Toplam Adet", "Mevcut Adet"
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

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(scrollTable, BorderLayout.CENTER);

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

        // Çarpıya basınca da geri dön
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        // Eventler
        btnAra.addActionListener(e -> kitapAra());

        // Otomatik arama (yazarken)
        txtKitapAdi.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
        });
        txtYazar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
        });
        txtBasimYilMin.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
        });
        txtBasimYilMax.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { kitapAra(); }
        });

        cmbKategori.addActionListener(e -> kitapAra());
        chkSadeceMevcut.addActionListener(e -> kitapAra());
        cmbSort.addActionListener(e -> kitapAra());

        // İlk yükleme
        kitapAra();
    }

    private void kitapAra() {
        tableModel.setRowCount(0);

        String kitapAdi = bosMu(txtKitapAdi.getText());
        String yazar = bosMu(txtYazar.getText());
        String kategori = cmbKategori.getSelectedItem().equals("Tümü") ? null : (String) cmbKategori.getSelectedItem();

        Integer minYil = parseIntGuvenli(txtBasimYilMin.getText());
        Integer maxYil = parseIntGuvenli(txtBasimYilMax.getText());

        boolean sadeceMevcut = chkSadeceMevcut.isSelected();

        String sortColumn = "kitapadi";
        String sortDir = "ASC";

        switch (cmbSort.getSelectedIndex()) {
            case 1 -> { sortColumn = "kitapadi"; sortDir = "DESC"; }
            case 2 -> { sortColumn = "yazar"; sortDir = "ASC"; }
            case 3 -> { sortColumn = "basimyili"; sortDir = "DESC"; }
            case 4 -> { sortColumn = "basimyili"; sortDir = "ASC"; }
            default -> { sortColumn = "kitapadi"; sortDir = "ASC"; }
        }

        String sql = "SELECT * FROM sp_KitapAra_Dinamik(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kitapAdi);
            ps.setString(2, yazar);
            ps.setString(3, kategori);
            ps.setObject(4, minYil);
            ps.setObject(5, maxYil);
            ps.setBoolean(6, sadeceMevcut);
            ps.setString(7, sortColumn);
            ps.setString(8, sortDir);

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
            JOptionPane.showMessageDialog(this, "Arama hatası:\n" + ex.getMessage(), "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String bosMu(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private Integer parseIntGuvenli(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}