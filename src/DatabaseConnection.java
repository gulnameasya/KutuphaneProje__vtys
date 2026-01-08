import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/kutuphane";
    private static final String USER = "postgres";
    private static final String PASSWORD = "11127"; // Test için kalsın, teslimde değiştir veya gizle

    // Nesne oluşturulmasını engelle
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Bu sınıf instantiate edilemez!");
    }

    /**
     * Veritabanı bağlantısını döner.
     * @return Connection nesnesi
     * @throws SQLException bağlantı hatası olursa
     */
    public static Connection getConnection() throws SQLException {
        try {
            // PostgreSQL JDBC driver otomatik yüklenir (Java 6+), Class.forName gerekmez
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // Bağlantı başarılıysa log (isteğe bağlı)
            System.out.println("Veritabanına başarıyla bağlanıldı: " + URL);
            return conn;
        } catch (SQLException e) {
            // Daha bilgilendirici hata mesajı
            System.err.println("Veritabanı bağlantı hatası!");
            System.err.println("URL: " + URL);
            System.err.println("Kullanıcı: " + USER);
            System.err.println("Hata mesajı: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            throw e; // Üst katmana fırlat
        }
    }

    // Opsiyonel: Bağlantıyı test etmek için main metodu (test amaçlı)
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            System.out.println("Bağlantı testi başarılı!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Bağlantı testi başarısız!");
        }
    }
}
