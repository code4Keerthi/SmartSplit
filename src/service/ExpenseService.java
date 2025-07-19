package service;

import model.Expense;
import model.Group;
import model.Repayment;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ExpenseService {
    private final List<Expense> expenses;
    private final List<Group> groups;

    public ExpenseService(List<Expense> expenses, List<Group> groups) {
        this.expenses = (expenses != null) ? expenses : new ArrayList<>();
        this.groups = (groups != null) ? groups : new ArrayList<>();
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void addExpense(Expense e) {
        expenses.add(e);
    }

    public void createGroup(Group g) {
        groups.add(g);
    }

    public Group getGroupById(String groupId) {
        for (Group g : groups) {
            if (g.getGroupId().equalsIgnoreCase(groupId)) return g;
        }
        return null;
    }

    private final List<Repayment> repayments = new ArrayList<>();

    public void recordRepayment(String fromUser, String toUser, double amount) {
        Repayment repayment = new Repayment(fromUser, toUser, amount, LocalDateTime.now());
        repayments.add(repayment);

        // ✅ Adjust debt after repayment
        updateBalancesAfterRepayment(fromUser, toUser, amount);

        // Add a repayment log to each relevant expense
        for (Expense e : expenses) {
            List<String> people = Arrays.asList(e.getParticipants());
            if (people.contains(fromUser) && people.contains(toUser)) {
                String log = "🔁 Repayment of ₹" + amount + " from " + fromUser + " to " + toUser +
                        " on " + repayment.getTimestamp().toLocalDate();
                e.getUpdateLogs().add(log);
            }
        }

        System.out.printf("✅ Repayment of ₹%.2f from %s to %s recorded.\n", amount, fromUser, toUser);

        saveRepaymentsToFile();  // Save repayment to file
    }



    public List<Expense> getGroupExpenses(String groupId) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (groupId.equals(e.getGroupId())) {
                result.add(e);
            }
        }
        return result;
    }

//    public void showGroupSummary(String groupId) {
//        Group group = getGroupById(groupId);
//        if (group == null) {
//            System.out.println("❌ Group not found.");
//            return;
//        }
//
//        // 1️⃣ Initialize netBalances
//        Map<String, Double> netBalances = new HashMap<>();
//        for (String member : group.getMembers()) {
//            netBalances.put(member, 0.0);
//        }
//
//        // 2️⃣ Process all expenses in this group
//        for (Expense e : getGroupExpenses(groupId)) {
//            List<String> participants = Arrays.asList(e.getParticipants());
//            double totalAmount = e.getTotalAmount();
//            double perHead = totalAmount / participants.size();
//
//            for (String participant : participants) {
//                netBalances.put(participant, netBalances.getOrDefault(participant, 0.0) - perHead);
//            }
//
//            for (Map.Entry<String, Double> entry : e.getPaidBy().entrySet()) {
//                String payer = entry.getKey();
//                double amount = entry.getValue();
//                netBalances.put(payer, netBalances.getOrDefault(payer, 0.0) + amount);
//            }
//        }
//
//        // 3️⃣ Apply repayments
//        // 3️⃣ Apply repayments safely
//        for (Repayment r1 : repayments) {
//            if (group.getMembers().contains(r1.getFromUser()) && group.getMembers().contains(r1.getToUser())) {
//                Double fromBal = netBalances.get(r1.getFromUser());
//                Double toBal = netBalances.get(r1.getToUser());
//
//                // 3️⃣ Apply repayments
//                for (Repayment r : repayments) {
//                    if (group.getMembers().contains(r.getFromUser()) && group.getMembers().contains(r.getToUser())) {
//                        netBalances.put(r.getFromUser(), netBalances.getOrDefault(r.getFromUser(), 0.0) - r.getAmount());
//                        netBalances.put(r.getToUser(), netBalances.getOrDefault(r.getToUser(), 0.0) + r.getAmount());
//                    }
//                }
//
//
//            }
//        }
//
//
//        // 4️⃣ Match debtors to creditors cleanly
//        System.out.println("\n\033[1;36mGroup Summary for " + group.getGroupName() + " (" + groupId + ")\033[0m");
//
//        PriorityQueue<Map.Entry<String, Double>> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
//        PriorityQueue<Map.Entry<String, Double>> debtors = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
//
//        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
//            double amount = entry.getValue();
//            if (Math.abs(amount) < 0.01) continue;
//            if (amount > 0) {
//                creditors.offer(Map.entry(entry.getKey(), amount));
//            } else {
//                debtors.offer(Map.entry(entry.getKey(), amount));
//            }
//        }
//
//        if (creditors.isEmpty() && debtors.isEmpty()) {
//            System.out.println("✅ All group members are settled up!");
//            return;
//        }
//
//        while (!debtors.isEmpty() && !creditors.isEmpty()) {
//            Map.Entry<String, Double> debtor = debtors.poll();
//            Map.Entry<String, Double> creditor = creditors.poll();
//
//            double debt = -debtor.getValue();
//            double credit = creditor.getValue();
//            double settled = Math.min(debt, credit);
//
//            System.out.printf("💸 %s owes %s: ₹%.2f\n", debtor.getKey(), creditor.getKey(), settled);
//
//            double newDebtorBal = debtor.getValue() + settled;
//            double newCreditorBal = creditor.getValue() - settled;
//
//            if (Math.abs(newDebtorBal) > 0.01)
//                debtors.offer(Map.entry(debtor.getKey(), newDebtorBal));
//            if (Math.abs(newCreditorBal) > 0.01)
//                creditors.offer(Map.entry(creditor.getKey(), newCreditorBal));
//        }
//    }



    // File path to save repayments
    private final String REPAYMENT_FILE = "data/repayments.txt";

    // Call this after every repayment
    public void saveRepaymentsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/repayments.txt"))) {
            for (Repayment r : repayments) {
                writer.printf("%s,%s,%.2f,%s%n",
                        r.getFromUser(),
                        r.getToUser(),
                        r.getAmount(),
                        r.getTimestamp());
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to save repayments: " + e.getMessage());
        }
    }




    public void loadRepaymentsFromFile() {
        repayments.clear();
        File file = new File(REPAYMENT_FILE);
        if (!file.exists()) {
            System.out.println("🔍 No repayment file found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String from = parts[0];
                    String to = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    LocalDateTime time = LocalDateTime.parse(parts[3]);
                    repayments.add(new Repayment(from, to, amount, time));

                    // ✅ Apply repayment logic immediately
                    updateBalancesAfterRepayment(from, to, amount);
                }
            }
            System.out.println("✅ Repayments loaded from file: " + repayments.size());
        } catch (IOException e) {
            System.out.println("❌ Failed to load repayments: " + e.getMessage());
        }
    }


    private void updateBalancesAfterRepayment(String fromUser, String toUser, double amount) {
        for (Expense e : expenses) {
            List<String> participants = Arrays.asList(e.getParticipants());
            if (!participants.contains(fromUser) || !participants.contains(toUser)) continue;

            String log = "🔁 Repayment of ₹" + amount + " from " + fromUser + " to " + toUser +
                    " loaded from file on " + LocalDate.now();
            e.getUpdateLogs().add(log);
        }
    }



    public List<Expense> getUserExpenses(String username) {
        List<Expense> userExpenses = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getPaidBy().containsKey(username) || Arrays.asList(e.getParticipants()).contains(username)) {
                userExpenses.add(e);
            }
        }
        return userExpenses;
    }


    public void showUserSummary(String username) {
        Map<String, Double> netBalances = new HashMap<>();

        for (Expense e : getUserExpenses(username)) {
            List<String> participants = Arrays.asList(e.getParticipants());
            double totalAmount = e.getTotalAmount();
            double perHead = totalAmount / participants.size();

            Map<String, Double> paidBy = e.getPaidBy();

            for (String participant : participants) {
                double paid = paidBy.getOrDefault(participant, 0.0);
                netBalances.put(participant, netBalances.getOrDefault(participant, 0.0) + paid - perHead);
            }
        }
        // ✅ Apply repayment adjustments
        // ✅ Apply repayment adjustments only for existing balances
        for (Repayment r : repayments) {
            if (netBalances.containsKey(r.getToUser())) {
                netBalances.put(r.getToUser(), netBalances.get(r.getToUser()) - r.getAmount());
            }
            if (netBalances.containsKey(r.getFromUser())) {
                netBalances.put(r.getFromUser(), netBalances.get(r.getFromUser()) + r.getAmount());
            }
        }


//        // Apply repayments to adjust balances
//        for (Repayment r : repayments) {
//            if (r.getFromUser().equals(username)) {
//                netBalances.put(r.getToUser(), netBalances.getOrDefault(r.getToUser(), 0.0) - r.getAmount());
//            } else if (r.getToUser().equals(username)) {
//                netBalances.put(r.getFromUser(), netBalances.getOrDefault(r.getFromUser(), 0.0) + r.getAmount());
//            }
//        }

        boolean any = false;
        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            String person = entry.getKey();
            double amount = entry.getValue();

            if (person.equals(username) || Math.abs(amount) < 0.01) continue;

            any = true;
            if (amount > 0) {
                System.out.printf("💸 You owe %s: ₹%.2f\n", person, amount);
            } else {
                System.out.printf("🤑 %s owes you: ₹%.2f\n", person, -amount);
            }
        }

        if (!any) {
            System.out.println("🎉 You have no balances to settle.");
        }
    }



    public void updateExpenseAmount(String expenseId, double newAmount, String updatedBy) {
        for (Expense e : expenses) {
            if (e.getId().equals(expenseId)) {
                double oldAmount = e.getTotalAmount();
                e.setTotalAmount(newAmount);

                String log = "Amount updated from ₹" + oldAmount + " to ₹" + newAmount +
                        " by " + updatedBy + " on " + LocalDate.now();
                e.getUpdateLogs().add(log);
                return;
            }
        }
    }

    public void updateExpensePaidBy(String expenseId, Map<String, Double> newPaidBy, String updatedBy) {
        for (Expense e : expenses) {
            if (e.getId().equals(expenseId)) {
                Map<String, Double> oldMap = e.getPaidBy();
                e.setPaidBy(newPaidBy);

                String log = "PaidBy updated by " + updatedBy + " on " + LocalDate.now();
                e.getUpdateLogs().add(log);
                return;
            }
        }
    }

    public void removeExpense(String id) {
        expenses.removeIf(e -> e.getId().equals(id));
    }

    public void updateExpense(Expense updatedExpense) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(updatedExpense.getId())) {
                Expense oldExpense = expenses.get(i);

                // Step 1: Add detailed update log
                StringBuilder log = new StringBuilder();
                log.append("🔄 Updated on ").append(LocalDate.now()).append("\n");
                log.append("→ Previous Amount: ₹").append(oldExpense.getTotalAmount()).append(", Paid By: ").append(oldExpense.getPaidBy()).append("\n");
                log.append("→ New Amount: ₹").append(updatedExpense.getTotalAmount()).append(", Paid By: ").append(updatedExpense.getPaidBy());
                updatedExpense.addUpdateLog(log.toString());

                // Step 2: Replace the old expense with new one
                expenses.set(i, updatedExpense);

                // Step 3: Recalculate all balances
                recalculateAllBalances();
                return;
            }
        }
    }

    private void recalculateAllBalances() {
    }


}
