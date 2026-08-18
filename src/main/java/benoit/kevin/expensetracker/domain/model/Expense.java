package benoit.kevin.expensetracker.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private int amount;

    protected Expense() {}

    public Expense(String type, int amount) {
        Objects.requireNonNull(type);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
    public Long getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    public int getAmount() {
        return amount;
    }
}