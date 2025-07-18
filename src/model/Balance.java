package model;

public class Balance {
    private String fromUser;
    private String toUser;
    private double amount;

    public Balance(String fromUser, String toUser, double amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }

    public String getFromUser() { return fromUser; }
    public String getToUser() { return toUser; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return "💸 " + fromUser + " owes " + toUser + ": ₹" + amount;
    }
}
