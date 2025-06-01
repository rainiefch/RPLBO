package todotodone.app.demo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_NAME = "todotodone.db";

    private static final String DB_URL = "jdbc:sqlite:" + new java.io.File(DB_NAME).getAbsolutePath();

    private static Connection connection = null;

    private DBConnection() {}

    // untuk mengambil koneksi database, dan akan membuat koneksi baru jika belum ada atau sudah tertutup.
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    // Dipakai untuk membuat koneksi baru ke database SQLite
    // Kalau driver SQLite tidak ditemukan atau gagal terhubung, akan muncul error.
    private static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite: " + DB_URL);
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
            throw new SQLException("JDBC Driver not found.", e);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }

    //untuk menutup koneksi ke database jika koneksi masih terbuka jika sudaha selesai digunakan
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
