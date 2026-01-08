import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;

public class CezaGoruntulemeFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<ComboItem> cmbUye;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public CezaGoruntulemeFrame() {
        setTitle("Ceza Görüntüleme");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Arka plan rengi
        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("CEZA GÖRÜNTÜLEME", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== ÜYE SEÇİM PANELİ =====
        JPanel panelUst = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelUst.setBackground(new Color(240, 255, 240));
        panelUst.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        panelUst.add(new JLabel("Üye Seç: "));
        DefaultComboBoxModel<ComboItem> modelUye = new DefaultComboBoxModel<>();
        modelUye.addElement(new ComboItem(0, "Tümü"));
        cmbUye = new JComboBox<>(modelUye);
        cmbUye.setPreferredSize(new Dimension(500, 50));
        cmbUye.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        panelUyeDoldur(modelUye);
        panelUst.add(cmbUye);

        add(panelUst, BorderLayout.NORTH);

        // ===== TABLO =====
        tableModel = new DefaultTableModel(new Object[]{
                "Ceza ID", "Üye Ad Soyad", "Kitap Adı", "Gecikme (Gün)", "Ceza Tutarı (TL)", "Oluşma Tarihi"
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
        scrollTable.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        add(scrollTable, BorderLayout.CENTER);

        // ===== ALT PANEL - GERİ DÖN BUTONU =====
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
            dispose(); // Bu pencereyi kapat
            AnaMenuFrame.showMenu(); // Ana menüyü tekrar göster
        });
        panelAlt.add(btnGeri);

        add(panelAlt, BorderLayout.SOUTH);

        // Çarpı (X) butonuna basınca da ana menüye dön
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        // Verileri yükle
        cmbUye.addActionListener(e -> cezaListele());
        cezaListele();
    }

    private void panelUyeDoldur(DefaultComboBoxModel<ComboItem> model) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UyeID, AdSoyad FROM UYE ORDER BY AdSoyad")) {
            while (rs.next()) {
                model.addElement(new ComboItem(rs.getLong("UyeID"), rs.getString("AdSoyad")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Üye yükleme hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cezaListele() {
        tableModel.setRowCount(0);
        ComboItem secili = (ComboItem) cmbUye.getSelectedItem();
        boolean filtre = secili != null && secili.getId() != 0;

        String sql = """
            SELECT c.CezaID, u.AdSoyad, k.KitapAdi, c.GecikmeGun, c.Tutar, c.OlusmaTarihi
            FROM CEZA c
            JOIN UYE u ON c.UyeID = u.UyeID
            JOIN ODUNC o ON c.OduncID = o.OduncID
            JOIN KITAP k ON o.KitapID = k.KitapID
            """ + (filtre ? " WHERE c.UyeID = ?" : "") + " ORDER BY c.OlusmaTarihi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtre) ps.setLong(1, secili.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getLong("CezaID"),
                            rs.getString("AdSoyad"),
                            rs.getString("KitapAdi"),
                            rs.getInt("GecikmeGun"),
                            rs.getBigDecimal("Tutar") + " TL",
                            DATE_FORMAT.format(rs.getTimestamp("OlusmaTarihi"))
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ceza listeleme hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}