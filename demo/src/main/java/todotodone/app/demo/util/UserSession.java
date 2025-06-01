package todotodone.app.demo.util;

//Kelas untuk menyimpan data sesi pengguna yang sedang login
public class UserSession {
    private static UserSession instance;// Menyimpan satu-satunya objek UserSession yang aktif
    private String username;
    private int userId;


    //
    private UserSession(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    //
    public static void initializeSession(String username, int userId) {
        instance = new UserSession(username, userId);
    }

    //
    public static UserSession getInstance() {
        return instance;
    }

    //
    public String getUsername() {
        return username;
    }

    //
    public int getUserId() {
        return userId;
    }

    // Menghapus sesi pengguna (dijalankan saat logout)
    public static void clearSession() {
        instance = null;
    }
}
