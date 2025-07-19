package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String name;
    private String email;
    private String currency;
    private List<String> categories;
    private String userId;
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public User(String username, String password, String name, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.currency = "₹"; // default
        this.categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Rent");
        categories.add("Travel");
        categories.add("Shopping");
        categories.add("Other");
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getCategories() {
        return categories;
    }

    public boolean checkPassword(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public boolean changePassword(String oldPass, String newPass) {
        if (!password.equals(oldPass)) {
            throw new IllegalArgumentException("Old password doesn't match.");
        }
        this.password = newPass;
        return true;
    }

    @Override
    public String toString() {
        return "👤 Username: " + username + ", Name: " + name + ", Email: " + email;
    }
}
