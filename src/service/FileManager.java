package service;

import model.Group;
import model.Expense;
import model.User;

import java.io.*;
import java.util.*;

public class FileManager {
    private static final String USER_FILE = "data/users.txt";
    private static final String EXPENSE_FILE = "data/expenses.txt";

    // USER FILE METHODS
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USER_FILE);
        if (!file.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    User user = new User(parts[0], parts[1], parts[2], parts[3]);
                    user.setCurrency(parts[4]);
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading users: " + e.getMessage());
        }

        return users;
    }

    public static void saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : users) {
                String line = String.join("|",
                        user.getUsername(),
                        user.getPassword(),
                        user.getName(),
                        user.getEmail(),
                        user.getCurrency()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving users: " + e.getMessage());
        }
    }

    // EXPENSE FILE METHODS
    public static List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(EXPENSE_FILE);
        if (!file.exists()) return expenses;

        try (BufferedReader br = new BufferedReader(new FileReader(EXPENSE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    String name = parts[0];
                    String desc = parts[1];
                    double amount = Double.parseDouble(parts[2]);

                    // Parse paidBy map from payer1:amount1,payer2:amount2
                    Map<String, Double> paidBy = new HashMap<>();
                    String[] payerPairs = parts[3].split(",");
                    for (String pair : payerPairs) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            String payer = kv[0];
                            double amt = Double.parseDouble(kv[1]);
                            paidBy.put(payer, amt);
                        }
                    }

                    String[] participants = parts[4].split(",");
                    String timestamp = parts[5];
                    String category = parts[6];
                    String groupId = parts[7];
                    if (groupId.equals("null")) groupId = null;

                    expenses.add(new Expense(name, desc, amount, paidBy, participants, timestamp, category, groupId));
                }


            }
        } catch (IOException e) {
            System.out.println("❌ Error reading expenses: " + e.getMessage());
        }

        return expenses;
    }

    public static void saveExpenses(List<Expense> expenses) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EXPENSE_FILE))) {
            for (Expense e : expenses) {
                // Convert Map<String, Double> to payer1:amount1,payer2:amount2
                StringBuilder paidByBuilder = new StringBuilder();
                for (Map.Entry<String, Double> entry : e.getPaidBy().entrySet()) {
                    if (paidByBuilder.length() > 0) paidByBuilder.append(",");
                    paidByBuilder.append(entry.getKey()).append(":").append(entry.getValue());
                }

                String line = String.join("|",
                        e.getName(),
                        e.getDescription(),
                        String.valueOf(e.getTotalAmount()),
                        paidByBuilder.toString(),
                        String.join(",", e.getParticipants()),
                        e.getTimestamp(),
                        e.getCategory(),
                        (e.getGroupId() == null ? "null" : e.getGroupId())
                );


                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving expenses: " + e.getMessage());
        }

    }
    // GROUP FILE METHODS
    public static List<Group> loadGroups() {
        List<Group> groups = new ArrayList<>();
        File file = new File("data/groups.txt");

        if (!file.exists()) return groups;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    Group group = new Group(parts[1]);  // groupName
                    // Manually set the groupId
                    try {
                        java.lang.reflect.Field idField = Group.class.getDeclaredField("groupId");
                        idField.setAccessible(true);
                        idField.set(group, parts[0]); // groupId
                    } catch (Exception e) {
                        System.out.println("⚠️ Reflection failed for group ID");
                    }

                    String[] members = parts[2].split(",");
                    for (String m : members) {
                        group.addMember(m.trim());
                    }

                    groups.add(group);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading groups: " + e.getMessage());
        }

        return groups;
    }

    public static void saveGroups(List<Group> groups) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/groups.txt"))) {
            for (Group group : groups) {
                String line = String.join("|",
                        group.getGroupId(),
                        group.getGroupName(),
                        String.join(",", group.getMembers())
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving groups: " + e.getMessage());
        }
    }

}
