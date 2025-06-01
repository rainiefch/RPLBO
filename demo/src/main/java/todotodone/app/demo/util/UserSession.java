package todotodone.app.demo.util;

//Kelas untuk menyimpan data sesi pengguna yang sedang login
public class UserSession {
    private static UserSession instance;// Menyimpan satu-satunya objek UserSession yang aktif
    private String username;
    private int userId;


    //Constructor ini cuma bisa dipanggil dari dalam kelas ini saja
    // Dipakai untuk membuat sesi baru
    private UserSession(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    // Digunakan saat login berhasil untuk menyimpan data user ke dalam ses
    public static void initializeSession(String username, int userId) {
        instance = new UserSession(username, userId);
    }

    //Digunakan untuk mengambil data sesi yang sedang aktif
    public static UserSession getInstance() {
        return instance;
    }

    //// Mengambil username dari sesi saat ini
    public String getUsername() {
        return username;
    }

    //Mengambil userId dari sesi saat ini
    public int getUserId() {
        return userId;
    }

    // Menghapus sesi pengguna (dijalankan saat logout)
    public static void clearSession() {
        instance = null;
    }
}
