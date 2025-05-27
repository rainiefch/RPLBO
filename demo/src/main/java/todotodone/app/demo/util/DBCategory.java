package todotodone.app.demo.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBCategory {
    private static DBCategory instance;

    private DBCategory() {}

    public static DBCategory getInstance() {
        if (instance == null) {
            instance = new DBCategory();
        }
        return instance;
    }

    public static class Category {
        private int id;
        private String name;
        private String description;

        public Category(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id_category, name_category, desc_category FROM category";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("id_category"),
                        rs.getString("name_category"),
                        rs.getString("desc_category")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}

