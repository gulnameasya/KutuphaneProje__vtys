import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class KitapTeslimFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtAra;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public KitapTeslimFrame() {
        setTitle("Kitap Teslim Alma");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("KİTAP TESLİM ALMA", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== ARAMA PANELİ =====
        JPanel panelAra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAra.setBackground(new Color(240, 255, 240));
        panelAra.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        panelAra.add(new JLabel("Arama (Üye Adı / Kitap Adı): "));
        txtAra = new JTextField(50);
        txtAra.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtAra.setPreferredSize(new Dimension(600, 50));
        panelAra.add(txtAra);

        JButton btnAra = new JButton("ARA");
        btnAra.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnAra.setBackground(new Color(0, 140, 0));
        btnAra.setForeground(Color.WHITE);
        btnAra.setPreferredSize(new Dimension(150, 50));
        btnAra.setFocusPainted(false);
        panelAra.add(btnAra);

        add(panelAra, BorderLayout.NORTH);

        // Gerçek zamanlı arama
        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { oduncListele(txtAra.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { oduncListele(txtAra.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { oduncListele(txtAra.getText().trim()); }
        });
        btnAra.addActionListener(e -> oduncListele(txtAra.getText().trim()));

        // ===== TABLO =====
        tableModel = new DefaultTableModel(new Object[]{
                "Ödünç ID", "Üye Ad Soyad", "Kitap Adı", "Yazar",
                "Ödünç Tarihi", "Son Teslim Tarihi", "Gecikme (Gün)"
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);

        // Tarih sütunlarını ortala
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Gecikme sütununu kırmızı ve kalın yap
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int gun = (int) value;
                c.setForeground(gun > 0 ? Color.RED : Color.BLACK);
                c.setFont(c.getFont().deriveFont(gun > 0 ? Font.BOLD : Font.PLAIN));
                ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(scrollTable, BorderLayout.CENTER);

        // ===== ALT PANEL =====
        JPanel panelAlt = new JPanel(new BorderLayout());
        panelAlt.setBackground(new Color(240, 255, 240));
        panelAlt.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

        // Teslim Al Butonu (sağda)
        JButton btnTeslimAl = new JButton("TESLİM AL");
        btnTeslimAl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        btnTeslimAl.setBackground(new Color(0, 140, 0));
        btnTeslimAl.setForeground(Color.WHITE);
        btnTeslimAl.setPreferredSize(new Dimension(350, 80));
        btnTeslimAl.setFocusPainted(false);
        btnTeslimAl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTeslimAl.addActionListener(e -> teslimAl());

        JPanel panelSag = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSag.add(btnTeslimAl);

        // Geri Dön Butonu (solda)
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

        JPanel panelSol = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSol.add(btnGeri);

        panelAlt.add(panelSol, BorderLayout.WEST);
        panelAlt.add(panelSag, BorderLayout.EAST);
        add(panelAlt, BorderLayout.SOUTH);

        // Çarpıya basınca da ana menüye dön
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AnaMenuFrame.showMenu();
            }
        });

        // İlk yükleme
        oduncListele("");
    }

    private void oduncListele(String arama) {
        tableModel.setRowCount(0);

        String sql = """
            SELECT 
                o.oduncid,
                u.adsoyad,
                k.kitapadi,
                k.yazar,
                o.odunctarihi,
                o.sonteslimtarihi,
                GREATEST(CURRENT_DATE - o.sonteslimtarihi, 0) AS gecikmegun
            FROM odunc o
            JOIN uye u ON o.uyeid = u.uyeid
            JOIN kitap k ON o.kitapid = k.kitapid
            WHERE o.teslimtarihi IS NULL
        """;

        if (!arama.isEmpty()) {
            sql += " AND (LOWER(u.adsoyad) LIKE LOWER(?) OR LOWER(k.kitapadi) LIKE LOWER(?))";
        }

        sql += " ORDER BY o.sonteslimtarihi ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!arama.isEmpty()) {
                String pattern = "%" + arama.toLowerCase() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getLong("oduncid"),
                            rs.getString("adsoyad"),
                            rs.getString("kitapadi"),
                            rs.getString("yazar"),
                            DATE_FORMAT.format(rs.getDate("odunctarihi")),
                            DATE_FORMAT.format(rs.getDate("sonteslimtarihi")),
                            rs.getInt("gecikmegun")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ödünç listeleme hatası:\n" + ex.getMessage(),
                    "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void teslimAl() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen teslim alınacak ödünç kaydını tablodan seçin!",
                    "Seçim Gerekli", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long oduncID = (long) tableModel.getValueAt(row, 0);
        int gecikmeGun = (int) tableModel.getValueAt(row, 6);

        int onay = JOptionPane.showConfirmDialog(this,
                "<html><b>Seçilen kitabı teslim almak istediğinize emin misiniz?</b><br><br>" +
                "Üye: <b>" + tableModel.getValueAt(row, 1) + "</b><br>" +
                "Kitap: <b>" + tableModel.getValueAt(row, 2) + "</b><br>" +
                (gecikmeGun > 0 ? "<font color='red'>Gecikme: " + gecikmeGun + " gün (Ceza eklenecek!)</font>" : "Zamanında teslim") + "</html>",
                "Teslim Onayı",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (onay != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall("CALL sp_KitapTeslimAl(?, ?)")) {
                cs.setLong(1, oduncID);
                cs.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                cs.executeUpdate();

                conn.commit();

                String mesaj = gecikmeGun > 0
                        ? "<html><b>Kitap teslim alındı!</b><br>Gecikme: " + gecikmeGun + " gün<br><font color='red'>Ceza başarıyla eklendi.</font></html>"
                        : "<html><b>Kitap zamanında teslim alındı!</b><br>Ceza uygulanmadı.</html>";

                JOptionPane.showMessageDialog(this, mesaj, "Başarılı", JOptionPane.INFORMATION_MESSAGE);

                oduncListele(txtAra.getText().trim());

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Teslim alma sırasında hata oluştu:\n" + ex.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}