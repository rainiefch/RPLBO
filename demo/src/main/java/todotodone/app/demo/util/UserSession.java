package todotodone.app.demo.util;

public class UserSession {
    private static UserSession instance;
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

    //Menyimpan satu-satunya instance (objek) UserSession, agar hanya ada satu session aktif
    public static UserSession getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public static void clearSession() {
        instance = null;
    }
}
