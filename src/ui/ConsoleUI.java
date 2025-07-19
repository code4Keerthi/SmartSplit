package ui;

import model.Expense;
import model.Group;
import model.User;
import service.ExpenseService;
import service.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConsoleUI {
    private Scanner sc = new Scanner(System.in);
    private List<User> users;
    private User currentUser;
    private ExpenseService expenseService;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleUI() {
        users = FileManager.loadUsers();
        for (User u : users) {
            if (u.getUserId() != null && u.getUserId().startsWith("USR")) {
                try {
                    int num = Integer.parseInt(u.getUserId().substring(3));
                    userCounter = Math.max(userCounter, num + 1);
                } catch (Exception ignored) {}
            }
        }
        List<Expense> expenses = FileManager.loadExpenses();
        List<Group> groups = FileManager.loadGroups();
        expenseService = new ExpenseService(expenses, groups);
        expenseService.loadRepaymentsFromFile();
    }

    public void start() {
//        System.out.println("✅ Startup Check:");
//        System.out.println("→ Expenses loaded: " + expenseService.getExpenses().size());
//        System.out.println("→ Repayments loaded: (You can’t access directly but can log from ExpenseService)");

        System.out.println("\033[1;35m\n===== 💡 WELCOME TO SMARTSPLIT 💡 =====");

        while (true) {
            System.out.println("\033[1;36m\n1. Sign Up 📝\n2. Login 🔐\n3. Exit 👋");
            System.out.print("\033[1;37mChoose option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    signUp();
                    break;
                case "2":
                    if (login()) showMainMenu();
                    break;
                case "3":
                    exit();
                    return;
                default:
                    System.out.println("\033[1;31m❌ Invalid choice! Try again.");
            }
        }
    }
    private static int userCounter = 1; // Load from file if needed

    public String generateUserId() {
        return String.format("USR%03d", userCounter++);
    }
//    private void signUp() {
//        System.out.println("\n\033[1;32m===== CREATE ACCOUNT =====");
//
//        System.out.print("\033[0;35mFull Name: \033[0;37m");
//        String name = sc.nextLine();
//
//        String email;
//        while (true) {
//            System.out.print("\033[0;35mEmail: \033[0;37m");
//            email = sc.nextLine().trim();
//            if (isValidEmail(email)) break;
//            System.out.println("\033[1;31m❌ Invalid email format. Please try again (e.g., abc@example.com)");
//            for(User u : users){
//                if(u.getEmail().equals(email)){
//                    System.out.println("\033[1;31m❌ User with this email already exists.");
//                }
//            }
//        }
//
//        System.out.print("\033[0;35mUsername: \033[0;37m");
//        String uname = sc.nextLine();
//
//        for (User u : users) {
//            if (u.getUsername().equals(uname)) {
//                System.out.println("\033[1;31m❌ Username already exists.");
//                return;
//            }
//        }
//
//        System.out.print("\033[0;35mPassword: \033[0;37m");
//        String pwd = sc.nextLine();
//
//        User user = new User(uname, pwd, name, email);
//        user.setUserId(generateUserId());
//        users.add(user);
//        FileManager.saveUsers(users);
//        System.out.println("\033[1;32m✅ Account created successfully!");
//    }
private void signUp() {
    System.out.println("\n\033[1;32m===== CREATE ACCOUNT =====");

    System.out.print("\033[0;35mFull Name: \033[0;37m");
    String name = sc.nextLine();

    String email;
    while (true) {
        System.out.print("\033[0;35mEmail: \033[0;37m");
        email = sc.nextLine().trim();

        if (!isValidEmail(email)) {
            System.out.println("\033[1;31m❌ Invalid email format. Please try again (e.g., abc@example.com)");
            continue;
        }

        boolean exists = false;
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                System.out.println("\033[1;31m❌ User with this email already exists.");
                exists = true;
                break;
            }
        }
        if (!exists) break;
    }

    System.out.print("\033[0;35mUsername: \033[0;37m");
    String uname = sc.nextLine();

    for (User u : users) {
        if (u.getUsername().equals(uname)) {
            System.out.println("\033[1;31m❌ Username already exists.");
            return;
        }
    }

    System.out.print("\033[0;35mPassword: \033[0;37m");
    String pwd = sc.nextLine();

    User user = new User(uname, pwd, name, email);
    user.setUserId(generateUserId());
    users.add(user);
    FileManager.saveUsers(users);
    System.out.println("\033[1;32m✅ Account created successfully!");
}
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") &&
                email.indexOf('@') < email.lastIndexOf('.') &&
                email.indexOf('@') > 0 &&
                email.length() >= 5;
    }

    private boolean login() {
        System.out.println("\n\033[1;32m===== LOGIN =====");
        System.out.print("\033[0;35mUsername: \033[0;37m");
        String uname = sc.nextLine();
        System.out.print("\033[0;35mPassword: \033[0;37m");
        String pwd = sc.nextLine();

        for (User user : users) {
            if (user.getUsername().equals(uname) && user.checkPassword(pwd)) {
                currentUser = user;
                System.out.println("\033[1;32m\n🔐 Welcome, " + user.getName() + "!");
                return true;
            }
        }
        System.out.println("\033[1;31m❌ Invalid credentials.");
        return false;
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n\033[1;35m===== MAIN MENU =====");
            System.out.println("\033[1;36m1. 💰 Add Expense");
            System.out.println("2. 📜 View Expense History");
            System.out.println("3. ✏️ Update/Delete Expense");
            System.out.println("4. 💳 View Balances");
            System.out.println("5. 💸 Record Repayment");     // <--- NEW
            System.out.println("6. 👥 Group Management");
            System.out.println("7. 🚪 Logout");

            System.out.print("\033[1;37mChoose option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1": addExpense(); break;
                case "2": viewHistory(); break;
                case "3": updateOrDeleteExpense(); break;
                case "4": viewBalance(); break;
                case "5": recordRepaymentFlow(); break;  // <--- NEW METHOD
                case "6": showGroupMenu(); break;
                case "7": currentUser = null; return;
                default: System.out.println("\033[1;31m❌ Invalid choice!");
            }


        }
    }
    private void addExpense() {
        System.out.println("\n\033[1;32m===== ADD EXPENSE =====");

        System.out.print("\033[0;35mExpense Name: \033[0;37m");
        String name = sc.nextLine();
        while(name.isEmpty()){
            System.out.println("Expense name should not be empty");
            name=sc.nextLine();
        }
        System.out.print("\033[0;35mDescription(optional): \033[0;37m");
        String desc = sc.nextLine();

        double amount = 0;
        while (true) {
            System.out.print("\033[0;35mAmount (₹): \033[0;37m");
            String input = sc.nextLine().trim();
            try {
                amount = Double.parseDouble(input);
                if (amount <= 0) {
                    System.out.println("\033[1;31m❌ Amount must be greater than zero.");
                } else {
                    break; // valid input
                }
            } catch (NumberFormatException e) {
                System.out.println("\033[1;31m❌ Invalid number. Please enter a valid amount (e.g., 250.50)");
            }
        }


        String[] participants;
        String groupId = null;

        System.out.print("\033[0;35mIs this a group expense? (y/n): \033[0;37m");
        String isGroup = sc.nextLine().trim().toLowerCase();

        if (isGroup.equals("y")) {
            List<Group> userGroups = new ArrayList<>();
            for (Group g : expenseService.getGroups()) {
                if (g.getMembers().contains(currentUser.getUsername())) {
                    userGroups.add(g);
                }
            }

            if (userGroups.isEmpty()) {
                System.out.println("\033[1;35m😶 You're not part of any groups.");
                System.out.print("\033[0;36m❓ Do you want to create a new group now? (y/n): ");
                String create = sc.nextLine().trim().toLowerCase();

                if (create.equals("y")) {
                    createGroup();
                    userGroups = new ArrayList<>();
                    for (Group g : expenseService.getGroups()) {
                        if (g.getMembers().contains(currentUser.getUsername())) {
                            userGroups.add(g);
                        }
                    }
                    if (userGroups.isEmpty()) {
                        System.out.println("❌ Still no groups found. Cancelling expense.");
                        return;
                    }
                } else {
                    System.out.println("❌ Expense cancelled.");
                    return;
                }
            }

            System.out.println("\033[1;36m\n📂 How do you want to proceed?");
            System.out.println("\033[1;35m1. 📁 Add to existing group");
            System.out.println("2. 🆕 Create a new group");
            System.out.print("Choose option: ");
            String groupChoice = sc.nextLine().trim();

            if (groupChoice.equals("2")) {
                createGroup();
            }

            listGroups();  // Show updated list after possible new group creation

            System.out.print("\033[0;35mEnter Group ID: \033[0;37m");
            groupId = sc.nextLine().trim();

            Group group = expenseService.getGroupById(groupId);
            if (group == null || !group.getMembers().contains(currentUser.getUsername())) {
                System.out.println("\033[1;31m❌ Invalid group ID or you're not a member.");
                return;
            }
            participants = group.getMembers().toArray(new String[0]);


            if (group == null) {
                System.out.println("\033[1;31m❌ Invalid group ID.");
                return;
            }
            participants = group.getMembers().toArray(new String[0]);

        } else {
            System.out.println("\033[1;36mHow do you want to record this?");
            System.out.println("1. 💼 Personal (just me)");
            System.out.println("2. 👤 Split with one person");
            System.out.println("3. 👥 Split with multiple users");
            System.out.print("\033[0;35mChoose option: \033[0;37m");
            String option = sc.nextLine().trim();

            if (option.equals("1")) {
                participants = new String[]{ currentUser.getUsername() }; // Only self
            } else if (option.equals("2")) {
                System.out.print("\033[0;35mEnter other user's name: \033[0;37m");
                String other = sc.nextLine().trim();
                participants = new String[]{ currentUser.getUsername(), other };
            } else if (option.equals("3")) {
                System.out.print("\033[0;35mEnter names (comma-separated): \033[0;37m");
                participants = sc.nextLine().split(",");
                for (int i = 0; i < participants.length; i++) {
                    participants[i] = participants[i].trim();
                }
            } else {
                System.out.println("\033[1;31m❌ Invalid choice. Cancelling expense.");
                return;
            }
        }

        System.out.print("\033[0;35mCategory (Food/Rent/Other): \033[0;37m");
        String category = sc.nextLine().trim();

        // Ask who paid
        Map<String, Double> paidBy = new HashMap<>();
        double paidTotal = 0;

        if (participants.length == 1 && participants[0].equals(currentUser.getUsername())) {
            // Personal Expense, current user paid
            paidBy.put(currentUser.getUsername(), amount);
        } else {
            // Ask who paid
            System.out.println("\033[1;34m💳 Enter payment breakdown:");
            System.out.println("  → Type each payment like: \033[0;36mme 300\033[0m or \033[0;36mvalli 250\033[0m");
            System.out.println("  → Type \033[0;35mdone\033[0m when finished or when total paid = ₹" + amount + "\n");

            double totalPaid = 0;

            while (true) {
                if (Math.abs(totalPaid - amount) < 0.01) break;

                System.out.print("\033[0;35mEnter (payer amount) or 'done': \033[0;37m");
                String line = sc.nextLine().trim();

                if (line.equalsIgnoreCase("done"  )) {
                    if (Math.abs(totalPaid - amount) < 0.01) break;
                    System.out.printf("\033[1;31m❌ Total paid so far: ₹%.2f. Still need ₹%.2f more.\n", totalPaid, amount - totalPaid);
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("\033[1;31m❌ Invalid format. Use: username amount");
                    continue;
                }
                String inputName = parts[0];
                String payer;

                if (inputName.equalsIgnoreCase("me")) {
                    payer = currentUser.getUsername();
                } else {
                    // Check by username or ID
                    User found = null;
                    for (User u : users) {
                        if (u.getUsername().equalsIgnoreCase(inputName) || u.getUserId().equalsIgnoreCase(inputName)) {
                            found = u;
                            break;
                        }
                    }

                    if (found == null) {
                        System.out.println("\033[1;31m❌ No such user found with username or ID: " + inputName);
                        continue;
                    }

                    // If it's a group expense, ensure they're in the group
                    if (groupId != null) {
                        Group group = expenseService.getGroupById(groupId);
                        if (group != null && !group.getMembers().contains(found.getUsername())) {
                            System.out.println("\033[1;31m❌ " + found.getUsername() + " is not in the group " + group.getGroupName());
                            continue;
                        }
                    }

                    payer = found.getUsername(); // map back to username
                }

                double amt;
                try {
                    amt = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    System.out.println("\033[1;31m❌ Invalid amount.");
                    continue;
                }

                if (groupId != null && !Arrays.asList(participants).contains(payer)) {
                    System.out.println("❌ " + payer + " is not a participant of this group.");
                    continue;
                }

                paidBy.put(payer, paidBy.getOrDefault(payer, 0.0) + amt);
                totalPaid += amt;
                if (totalPaid > amount) {
                    System.out.println("❌ Total paid exceeds the required amount.");
                    System.out.print("🔁 Do you want to restart payment entry? (y/n): ");
                    String restart = sc.nextLine().trim().toLowerCase();
                    if (restart.equals("y")) {
                        paidBy.clear();
                        totalPaid = 0;
                        continue; // restart loop from top
                    } else {
                        System.out.println("❌ Cannot proceed with overpayment. Try again.");
                        return; // or break and exit expense flow
                    }
                }
                System.out.printf("\033[1;32m✔ Recorded: %s paid ₹%.2f (Total Paid: ₹%.2f)\n", payer, amt, totalPaid);

            }

            if (Math.abs(totalPaid - amount) > 0.01) {
                System.out.println("\033[1;31m❌ Final mismatch. Total paid ₹" + totalPaid + " doesn’t match ₹" + amount);
                return;
            }

        }


        String timestamp = dtf.format(LocalDateTime.now());

        Expense expense = new Expense(name, desc, amount, paidBy, participants, timestamp, category, groupId);
        expenseService.addExpense(expense);
        FileManager.saveExpenses(expenseService.getExpenses());

        System.out.println("\033[1;32m✅ Expense added successfully!");
    }

    private void viewHistory() {
        System.out.println("\n\033[1;35m=== YOUR EXPENSE HISTORY ===");

        List<Expense> expenses = expenseService.getUserExpenses(currentUser.getUsername());

        if (expenses.isEmpty()) {
            System.out.println("🕳️ No expenses found.");
            return;
        }

        for (Expense e : expenses) {
            System.out.println("\n\033[1;36mExpense: " + e.getName());
            System.out.println("💵 Amount: ₹" + e.getTotalAmount());
            System.out.println("🕓 Created on: " + e.getTimestamp());
            System.out.println("👥 Participants: " + Arrays.toString(e.getParticipants()));
            System.out.println("💰 Paid by: " + e.getPaidBy());
            System.out.println("📂 Category: " + e.getCategory());

            List<String> logs = e.getUpdateLogs();
            if (!logs.isEmpty()) {
                System.out.println("🕓 Updated History(Logs):");
                for (String log : logs) {
                    System.out.println("   🔸 " + log);
                }
            }

            // Show update logs if available
//            if (e.getUpdateLogs() != null && !e.getUpdateLogs().isEmpty()) {
//                System.out.println("📝 Update History:");
//                for (String log : e.getUpdateLogs()) {
//                    System.out.println("   • " + log);
//                }
//            }

            System.out.println("--------------------------------------------------");
        }
    }


    private void updateOrDeleteExpense() {
        List<Expense> userExpenses = expenseService.getUserExpenses(currentUser.getUsername());

        if (userExpenses.isEmpty()) {
            System.out.println("🕳️ No expenses to manage.");
            return;
        }

        System.out.println("\n\033[1;36m🔍 Your Expenses:");
        for (int i = 0; i < userExpenses.size(); i++) {
            Expense e = userExpenses.get(i);
            System.out.printf("\n%d. %s | ₹%.2f | %s\n", i + 1, e.getName(), e.getTotalAmount(), e.getTimestamp());
        }

        System.out.print("\n\033[0;35mChoose expense number to manage (or 0 to cancel): \033[0;37m");
        int choice = Integer.parseInt(sc.nextLine());

        if (choice < 1 || choice > userExpenses.size()) {
            System.out.println("❌ Cancelled.");
            return;
        }

        Expense selected = userExpenses.get(choice - 1);

        System.out.println("\n1. ✏️ Update Expense");
        System.out.println("2. 🗑️ Delete Expense");
        System.out.print("\033[0;34mChoose option: ");
        String action = sc.nextLine().trim();

        if (action.equals("1")) {
            updateExpenseFlow(selected);
        } else if (action.equals("2")) {
            deleteExpenseFlow(selected);
        } else {
            System.out.println("❌ Invalid option. Aborting.");
        }
    }

    private void updateExpenseFlow(Expense expense) {
        System.out.println("\n\033[1;33m✏️ Updating expense: " + expense.getName());
        System.out.printf("\033[0;36mOld amount: ₹%.2f | Created: %s\n\n", expense.getTotalAmount(), expense.getTimestamp());

        // Re-enter paidBy map
        System.out.println("💳 \033[1;34mEnter updated payment breakdown:");
        System.out.println("  → Type like: \033[0;36mme 300\033[0m or \033[0;36mvalli 250\033[0m");
        System.out.println("  → Type \033[0;35mdone\033[0m when finished.\n");

        Map<String, Double> newPaidBy = new HashMap<>();
        double totalPaid = 0;

        while (true) {
            System.out.print("\033[0;35mEnter (payer amount) or 'done': \033[0;37m");
            String line = sc.nextLine().trim();

            if (line.equalsIgnoreCase("done")) break;

            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("\033[1;31m❌ Invalid format. Use: username amount");
                continue;
            }

            String payer = parts[0].equalsIgnoreCase("me") ? currentUser.getUsername() : parts[0];
            double amt;
            try {
                amt = Double.parseDouble(parts[1]);
                if (amt <= 0) {
                    System.out.println("\033[1;31m❌ Amount must be positive.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("\033[1;31m❌ Invalid amount.");
                continue;
            }

            newPaidBy.put(payer, newPaidBy.getOrDefault(payer, 0.0) + amt);
            totalPaid += amt;

            System.out.printf("\033[1;32m✔ Recorded: %s paid ₹%.2f (Total Paid: ₹%.2f)\n", payer, amt, totalPaid);
        }

        System.out.printf("\n\033[1;36mNew Total Amount: ₹%.2f\033[0m\n", totalPaid);

        System.out.print("✅ Do you want to save these changes? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("❌ Update cancelled.");
            return;
        }

        double oldAmount = expense.getTotalAmount();
        Map<String, Double> oldPaidBy = new HashMap<>(expense.getPaidBy());

        expense.setPaidBy(newPaidBy);
        expense.setTotalAmount(totalPaid);

        // Update the expense object

        StringBuilder logEntry = new StringBuilder();
        logEntry.append("Updated on ").append(dtf.format(LocalDateTime.now())).append("\n");
        logEntry.append("→ Previous Amount: ₹").append(oldAmount).append(", Paid By: ").append(oldPaidBy).append("\n");
        logEntry.append("→ New Amount: ₹").append(totalPaid).append(", Paid By: ").append(newPaidBy);

        expense.appendUpdateLog(logEntry.toString());


        // Save
        expenseService.updateExpense(expense);
        FileManager.saveExpenses(expenseService.getExpenses());

        System.out.println("✅ Expense updated successfully!");

        if (totalPaid == 0) {
            System.out.print("\nℹ️ This expense is now settled. Delete it permanently? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                expenseService.removeExpense(expense.getId());
                FileManager.saveExpenses(expenseService.getExpenses());
                System.out.println("🗑️ Expense deleted.");
            } else {
                System.out.println("🗃️ Settled expense kept for logs.");
            }
        }
    }


    private void deleteExpenseFlow(Expense selected) {
        List<Expense> userExpenses = expenseService.getUserExpenses(currentUser.getUsername());

        if (userExpenses.isEmpty()) {
            System.out.println("\033[1;33m🕳 No expenses to delete.");
            return;
        }

        System.out.println("\033[1;36mHere are your recent expenses:");
        for (int i = 0; i < userExpenses.size(); i++) {
            System.out.println("\033[0;37m[" + (i + 1) + "] " + userExpenses.get(i).getName() + " | ₹" + userExpenses.get(i).getTotalAmount());
        }

        System.out.print("\033[0;35mEnter the number of the expense to delete (or 0 to cancel): \033[0;37m");
        int idx = -1;
        try {
            idx = Integer.parseInt(sc.nextLine().trim());
            if (idx == 0) {
                System.out.println("❌ Deletion cancelled.");
                return;
            }
            if (idx < 1 || idx > userExpenses.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        Expense toDelete = userExpenses.get(idx - 1);
        System.out.print("\033[0;31m⚠️ Are you sure you want to permanently delete \"" + toDelete.getName() + "\"? (y/n): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("y")) {
            expenseService.removeExpense(toDelete.getId());
            FileManager.saveExpenses(expenseService.getExpenses());
            System.out.println("🗑️ Expense deleted.");
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    private void viewBalance() {
        System.out.println("\n\033[1;35m=== YOUR BALANCES ===");
        expenseService.showUserSummary(currentUser.getUsername());
    }

    private void recordRepaymentFlow() {
        System.out.println("\n\033[1;35m===== 💸 RECORD REPAYMENT =====");

        System.out.println("1. I repaid someone");
        System.out.println("2. Someone repaid me");
        System.out.print("\033[0;35mChoose option (1 or 2): \033[0;37m");
        String option = sc.nextLine().trim();

        boolean isPayingSomeone = option.equals("1");
        boolean isReceiving = option.equals("2");

        if (!isPayingSomeone && !isReceiving) {
            System.out.println("\033[1;31m❌ Invalid option.");
            return;
        }


        List<Expense> repaymentsToAdd = new ArrayList<>();

        while (true) {
            String fromUser, toUser;

            if (isPayingSomeone) {
                fromUser = currentUser.getUsername();
                System.out.print("\033[0;35mEnter username or ID you repaid to: \033[0;37m");
                toUser = sc.nextLine().trim();
                User toUserObj = getUserByUsernameOrId(toUser);
                if (toUserObj == null) {
                    System.out.println("\033[1;31m❌ No such user found.");
                    continue;
                }
                toUser = toUserObj.getUsername();

            } else {
                toUser = currentUser.getUsername();
                System.out.print("\033[0;35mEnter username who repaid you: \033[0;37m");
                fromUser = sc.nextLine().trim();
                User toUserObj = getUserByUsernameOrId(fromUser);
                if (toUserObj == null) {
                    System.out.println("\033[1;31m❌ No such user found.");
                    continue;
                }
                toUser = toUserObj.getUsername();

            }

            if (fromUser.equalsIgnoreCase(toUser)) {
                System.out.println("\033[1;31m❌ Sender and receiver cannot be same.");
                continue;
            }

            double amount;
            while (true) {
                System.out.print("\033[0;35mEnter repayment amount (₹): \033[0;37m");
                String input = sc.nextLine().trim();
                try {
                    amount = Double.parseDouble(input);
                    if (amount <= 0) {
                        System.out.println("\033[1;31m❌ Amount must be greater than zero.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\033[1;31m❌ Invalid number.");
                }
            }

            System.out.printf("\033[1;36mConfirm repayment of ₹%.2f from %s to %s? (y/n): \033[0;37m", amount, fromUser, toUser);
            String confirm = sc.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println("❌ Skipped this repayment.");
            } else {
                expenseService.recordRepayment(fromUser, toUser, amount);
                System.out.println("\033[1;32m✅ Repayment recorded.");
            }

            System.out.print("\033[0;35mDo you want to record another repayment? (y/n): \033[0;37m");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) break;
        }

        FileManager.saveExpenses(expenseService.getExpenses());
        System.out.println("\033[1;32m💾 All repayments saved.");
    }

    private void showGroupMenu() {
        while (true) {
            System.out.println("\n\033[1;35m===== GROUP MENU =====");
            System.out.println("\033[1;36m1. ➕ Create Group");
            System.out.println("2. 📋 View My Groups");
            //System.out.println("3. 📊 Group Summary");
            System.out.println("3. 🔙 Back to Main Menu");
            System.out.print("\033[1;37mChoose option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": createGroup(); break;
                case "2": listGroups(); break;
                //case "3": viewGroupSummary(); break;
                case "3": return;
                default: System.out.println("\033[1;31m❌ Invalid choice!");
            }
        }
    }

    private void createGroup() {
        System.out.print("\033[0;35mEnter group name: \033[0;37m");
        String name = sc.nextLine();

        Group group = new Group(name);
        group.addMember(currentUser.getUsername());
//
//        System.out.print("\033[0;35mAdd members (comma-separated usernames): \033[0;37m");
//        String[] members = sc.nextLine().split(",");
//        for (String m : members) {
//            String member = m.trim();
//            if (member.equalsIgnoreCase("me")) {
//                member = currentUser.getUsername();
//            }
//            group.addMember(member);
//        }
        System.out.print("\033[0;35mAdd members (comma-separated usernames or IDs): \033[0;37m");
        String[] members = sc.nextLine().split(",");

        for (String m : members) {
            String memberInput = m.trim();
            if (memberInput.equalsIgnoreCase("me")) {
                memberInput = currentUser.getUsername();
            }

            User matched = null;
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(memberInput) || u.getUserId().equalsIgnoreCase(memberInput)) {
                    matched = u;
                    break;
                }
            }

            if (matched == null) {
                System.out.println("\033[1;31m❌ No user found with: " + memberInput + ". Skipping.");
                continue;
            }

            if (group.getMembers().contains(matched.getUsername())) {
                System.out.println("\033[1;33m⚠️ " + matched.getUsername() + " already added.");
            } else {
                group.addMember(matched.getUsername());
                System.out.println("\033[1;32m✔ Added: " + matched.getUsername() + " (" + matched.getUserId() + ")");
            }
        }



        expenseService.createGroup(group);
        FileManager.saveGroups(expenseService.getGroups());

        System.out.println("\033[1;32m✅ Group created with ID: " + group.getGroupId());
    }

    private User getUserByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    private void listGroups() {
        List<Group> groups = expenseService.getGroups();
        System.out.println("\n\033[1;36mYour Groups:");
        boolean found = false;

        for (Group g : groups) {
            if (g.getMembers().contains(currentUser.getUsername())) {
                System.out.println("👥 Group: " + g.getGroupName() + " (" + g.getGroupId() + ")");
                System.out.println("    Members:");
                for (String username : g.getMembers()) {
                    User u = getUserByUsername(username);
                    if (u != null) {
                        System.out.printf("    - %s (%s)\n", u.getUsername(), u.getUserId());
                    } else {
                        System.out.printf("    - %s (❌ Unknown User)\n", username);
                    }
                }
                System.out.println();
                found = true;
            }
        }
//        for (Group g : groups) {
//            if (g.getMembers().contains(currentUser.getUsername())) {
//                System.out.print("👥 Group: " + g.getName() + " (" + g.getGroupId() + ") - Members: [");
//
//                List<String> userDetails = new ArrayList<>();
//                for (String uname : g.getMembers()) {
//                    User matched = null;
//                    for (User u : users) {
//                        if (u.getUsername().equals(uname)) {
//                            matched = u;
//                            break;
//                        }
//                    }
//
//                    if (matched != null) {
//                        userDetails.add(matched.getUsername() + " (" + matched.getUserId() + ")");
//                    } else {
//                        userDetails.add(uname + " (Unknown ID)");
//                    }
//                }
//
//                System.out.println(String.join(", ", userDetails) + "]");
//                found = true;
//            }
//        }



        if (!found) System.out.println("😶 You're not part of any groups.");
    }

//    private void viewGroupSummary() {
//        listGroups();
//        System.out.print("\033[0;35mEnter Group ID to view summary: \033[0;37m");
//        String gid = sc.nextLine();
//        //expenseService.showGroupSummary(gid);
//    }


    private User getUserByUsernameOrId(String input) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(input) || u.getUserId().equalsIgnoreCase(input)) {
                return u;
            }
        }
        return null;
    }

    private void exit() {
        System.out.println("\033[1;32m🖐 Exiting... Saving data.");
        FileManager.saveUsers(users);
        FileManager.saveExpenses(expenseService.getExpenses());
        FileManager.saveGroups(expenseService.getGroups());
        System.out.println("\033[1;32mGoodbye!");
    }
}
