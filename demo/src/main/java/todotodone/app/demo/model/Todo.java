package todotodone.app.demo.model;

public class Todo {
    private Integer id;
    private String title;
    private String status;
    private String category;
    private String dueDate;
    private String description;
    private String attachment;
    private int userId;

    public Todo() {}

    public Todo(Integer id, String title, String status, String category, String dueDate,
                String description, String attachment, int userId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.category = category;
        this.dueDate = dueDate;
        this.description = description;
        this.attachment = attachment;
        this.userId = userId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
