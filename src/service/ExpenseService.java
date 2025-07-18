package service;

import model.Expense;
import model.Group;
import model.Repayment;

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

        // Also add a repayment entry to all expenses that involve both users
        for (Expense e : expenses) {
            List<String> people = Arrays.asList(e.getParticipants());
            if (people.contains(fromUser) && people.contains(toUser)) {
                String log = "🔁 Repayment of ₹" + amount + " from " + fromUser + " to " + toUser +
                        " on " + repayment.getTimestamp().toLocalDate();
                e.getUpdateLogs().add(log);
            }
        }

        System.out.printf("✅ Repayment of ₹%.2f from %s to %s recorded.\n", amount, fromUser, toUser);
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

    public void showGroupSummary(String groupId) {
        Group group = getGroupById(groupId);
        if (group == null) {
            System.out.println("❌ Group not found.");
            return;
        }

        // ✅ Pairwise balances: balances[from][to] = how much `from` owes `to`
        Map<String, Map<String, Double>> balances = new HashMap<>();

        for (Expense e : getGroupExpenses(groupId)) {
            List<String> participants = Arrays.asList(e.getParticipants());
            double share = e.getTotalAmount() / participants.size();

            for (String payer : e.getPaidBy().keySet()) {
                double paidAmount = e.getPaidBy().get(payer);

                for (String participant : participants) {
                    if (participant.equals(payer)) continue;

                    double individualShare = share;
                    balances.putIfAbsent(participant, new HashMap<>());
                    Map<String, Double> owesMap = balances.get(participant);
                    owesMap.put(payer, owesMap.getOrDefault(payer, 0.0) + individualShare);
                }
            }
        }
        for (Repayment r : repayments) {
            String from = r.getFromUser();
            String to = r.getToUser();
            double amount = r.getAmount();

            if (group.getMembers().contains(from) && group.getMembers().contains(to)) {
                balances.putIfAbsent(from, new HashMap<>());
                Map<String, Double> owesMap = balances.get(from);
                owesMap.put(to, owesMap.getOrDefault(to, 0.0) - amount);
            }
        }


        // Apply repayments that occurred within the group
//        for (Repayment r : repayments) {
//            if (group.getMembers().contains(r.getFromUser()) && group.getMembers().contains(r.getToUser())) {
//                balances.put(r.getFromUser(), balances.getOrDefault(r.getFromUser(), 0.0) - r.getAmount());
//                balances.put(r.getToUser(), balances.getOrDefault(r.getToUser(), 0.0) + r.getAmount());
//            }
//        }

  //      System.out.println("\n\033[1;36mGroup Summary for " + group.getGroupName() + " (" + groupId + ")");

        // Step 2: Split creditors and debtors
//        PriorityQueue<Map.Entry<String, Double>> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
//        PriorityQueue<Map.Entry<String, Double>> debtors = new PriorityQueue<>((a, b) -> Double.compare(a.getValue(), b.getValue()));
//
//        for (Map.Entry<String, Map<String, Double>> entry : balances.entrySet()) {
//            double amount = entry.getValue().size();
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

        // Step 3: Match debts
        System.out.println("\n\033[1;36mGroup Summary for " + group.getGroupName() + " (" + groupId + ")");
        boolean anyOwed = false;

        for (String from : balances.keySet()) {
            for (Map.Entry<String, Double> entry : balances.get(from).entrySet()) {
                String to = entry.getKey();
                double amount = entry.getValue();
                if (amount > 0.01) {
                    //Math.round(amount * 100.0) / 100.0
                    anyOwed = true;
                    System.out.printf("💸 %s owes %s: ₹%.2f\n", from, to, amount);
                }
            }
        }

        if (!anyOwed) {
            System.out.println("🎉 All group members are settled up!");
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
        for (Repayment r : repayments) {
            if (r.getFromUser().equals(username)) {
                // You paid someone → you owe more
                netBalances.put(r.getToUser(), netBalances.getOrDefault(r.getToUser(), 0.0) - r.getAmount());
            } else if (r.getToUser().equals(username)) {
                // Someone paid you → they owe you less
                netBalances.put(r.getFromUser(), netBalances.getOrDefault(r.getFromUser(), 0.0) + r.getAmount());
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
