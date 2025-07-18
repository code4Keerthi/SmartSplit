package model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class Repayment {
    private String fromUser;
    private String toUser;
    private double amount;
    private LocalDateTime time;
    private LocalDateTime timestamp;

    public Repayment(String fromUser, String toUser, double amount, LocalDateTime now) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "💸 " + fromUser + " paid " + toUser + " ₹" + amount + " on " + time.toString();
    }

    public LocalDateTime getTimestamp() {
        // Return timestamp if not null, else fallback to 'time'
        return timestamp != null ? timestamp : time;
    }

}
