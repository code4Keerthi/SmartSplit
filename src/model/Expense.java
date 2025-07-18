package model;

import java.util.*;

public class Expense {
    private String name;
    private String description;
    private double totalAmount;
    private Map<String, Double> paidBy; // multiple payers
    private String[] participants;
    private String timestamp;
    private String category;
    private String groupId;
    private List<String> updateLogs;
    private final String id;
    public enum ExpenseType {
        SHARED,
        REPAYMENT
    }
    private ExpenseType expenseType = ExpenseType.SHARED; // default
    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public Expense(String name, String description, double totalAmount,
                   Map<String, Double> paidBy, String[] participants,
                   String timestamp, String category, String groupId) {

        this.id = UUID.randomUUID().toString(); // Generate unique ID for each expense
        this.name = name;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.participants = participants;
        this.timestamp = timestamp;
        this.category = category;
        this.groupId = groupId;
        this.updateLogs = new ArrayList<>();
        this.updateLogs.add("Created on " + timestamp);

    }

    public List<String> getUpdateLogs() {
        return updateLogs;
    }

    public String getId() {
        return id;
    }

    public void appendUpdateLog(String log) {
        updateLogs.add(log);
    }

    public void addUpdateLog(String logEntry) {
        updateLogs.add(logEntry);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Double> getPaidBy() {
        return paidBy;
    }

    public String[] getParticipants() {
        return participants;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPaidBy(Map<String, Double> paidBy) {
        this.paidBy = paidBy;
    }

    public void setParticipants(String[] participants) {
        this.participants = participants;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isSettled() {
        double perPersonShare = totalAmount / participants.length;
        Map<String, Double> netBalances = new HashMap<>();

        // Step 1: Initialize each participant's balance as -share
        for (String p : participants) {
            netBalances.put(p, -perPersonShare);
        }

        // Step 2: Add what they paid
        for (Map.Entry<String, Double> entry : paidBy.entrySet()) {
            String payer = entry.getKey();
            double amount = entry.getValue();
            netBalances.put(payer, netBalances.getOrDefault(payer, 0.0) + amount);
        }

        // Step 3: Check if everyone's balance is ≈ 0
        for (double bal : netBalances.values()) {
            if (Math.abs(bal) > 0.01) {
                return false; // Not settled
            }
        }

        return true; // Fully settled
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\033[1;35mExpense: ").append(name)
                .append("\n\033[0;36mDescription: ").append(description)
                .append("\nTotal Amount: ₹").append(totalAmount)
                .append("\nPaid By:\n");

        for (Map.Entry<String, Double> entry : paidBy.entrySet()) {
            builder.append("  → ").append(entry.getKey()).append(": ₹").append(entry.getValue()).append("\n");
        }

        builder.append("Participants: ").append(Arrays.toString(participants));
        builder.append("\nCategory: ").append(category);
        builder.append("\nCreated On: ").append(timestamp);
        builder.append("\nGroup: ").append(groupId == null ? "(Personal)" : groupId);

        if (!updateLogs.isEmpty()) {
            builder.append("\n\n\033[1;34m📜 Update History:");
            for (String log : updateLogs) {
                builder.append("\n  • ").append(log);
            }
        }

        builder.append("\n\033[0m");
        return builder.toString();
    }

}
