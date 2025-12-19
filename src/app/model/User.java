package app.model;

public class User {
    private int id;
    private String username;
    private String password; // stored as plain text in your current DB (see note below)
    private String role;
    private int failedAttempts;
    private boolean locked;

    public User() {}

    public User(int id, String username, String password, String role) {
        this(id, username, password, role, 0, false);
    }

    public User(int id, String username, String password, String role, int failedAttempts, boolean locked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}
