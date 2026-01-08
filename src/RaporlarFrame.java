import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Vector;

public class RaporlarFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    // Güvenli view listesi
    private static final Set<String> ALLOWED_VIEWS = Set.of(
            "vw_tarihbazlioduncraporu",
            "vw_gecikenkitaplarraporu",
            "vw_encokoduncalinankitaplar"
    );

    public RaporlarFrame() {
        setTitle("Raporlar ve İstatistikler");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240, 255, 240));

        // ===== BAŞLIK =====
        JLabel lblBaslik = new JLabel("RAPORLAR VE İSTATİSTİKLER", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblBaslik.setForeground(new Color(0, 100, 0));
        lblBaslik.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        add(lblBaslik, BorderLayout.NORTH);

        // ===== TABBED PANE =====
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setBackground(new Color(240, 255, 240));

        tabbedPane.addTab("Tarih Bazlı Ödünç Raporu", raporPanel("vw_tarihbazlioduncraporu"));
        tabbedPane.addTab("Geciken Kitaplar Raporu", raporPanel("vw_gecikenkitaplarraporu"));
        tabbedPane.addTab("En Çok Ödünç Alınan Kitaplar", raporPanel("vw_encokoduncalinankitaplar"));

        add(tabbedPane, BorderLayout.CENTER);

        // ===== ALT PANEL - GERİ DÖN =====
        JPanel panelAlt = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAlt.setBackground(new Color(240, 255, 240));
        panelAlt.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

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
    }

    private JScrollPane raporPanel(String viewName) {

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(0, 120, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(40);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        if (!ALLOWED_VIEWS.contains(viewName.toLowerCase())) {
            model.setColumnIdentifiers(new Object[]{"Hata"});
            model.addRow(new Object[]{"Yetkisiz rapor erişimi!"});
            return new JScrollPane(table);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + viewName)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                String colName = meta.getColumnLabel(i);
                columnNames.add(beautify(colName));
            }
            model.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);

                    if (value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                        value = DATE_FORMAT.format(value);
                    } else if (value == null) {
                        value = "-";
                    }

                    row.add(value);
                }
                model.addRow(row);
            }

            // Sayı ve tarih sütunlarını ortala
            for (int i = 0; i < columnCount; i++) {
                String header = columnNames.get(i).toLowerCase();
                if (header.contains("tarih") || header.contains("gün") || header.contains("adet") || header.contains("sıra") || header.contains("id")) {
                    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Rapor yüklenirken hata oluştu:\n" + viewName + "\n\n" + ex.getMessage(),
                    "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);

            model.setColumnIdentifiers(new Object[]{"Hata"});
            model.addRow(new Object[]{"Rapor verileri alınamadı."});
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        return scroll;
    }

    private String beautify(String column) {
        return switch (column.toLowerCase()) {
            case "adsoyad" -> "Üye Ad Soyad";
            case "kitapadi" -> "Kitap Adı";
            case "odunctarihi" -> "Ödünç Tarihi";
            case "sonteslimtarihi" -> "Son Teslim Tarihi";
            case "teslimtarihi" -> "Teslim Tarihi";
            case "gecikmegun" -> "Gecikme (Gün)";
            case "toplamodunc" -> "Toplam Ödünç Sayısı";
            case "sira" -> "Sıra";
            default -> column.replace("_", " ")
                             .substring(0, 1).toUpperCase() +
                             column.replace("_", " ").substring(1).toLowerCase();
        };
    }
}