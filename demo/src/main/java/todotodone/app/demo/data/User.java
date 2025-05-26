package todotodone.app.demo.data;

public class User {
    private int idUser;
    private String username;

    private static User currentUser; // static field untuk menyimpan user yang sedang login

    public User(int idUser, String username) {
        this.idUser = idUser;
        this.username = username;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getUsername() {
        return username;
    }

    // Static getter dan setter
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
