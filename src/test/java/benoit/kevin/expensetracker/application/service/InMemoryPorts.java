package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.out.BudgetPort;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.application.port.out.PasswordHasher;
import benoit.kevin.expensetracker.application.port.out.TokenIssuer;
import benoit.kevin.expensetracker.application.port.out.TransactionPort;
import benoit.kevin.expensetracker.application.port.out.UserPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.transaction.Expense;
import benoit.kevin.expensetracker.domain.transaction.Income;
import benoit.kevin.expensetracker.domain.transaction.Refund;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import benoit.kevin.expensetracker.domain.user.User;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The point of having driven ports: the services can be exercised with no Spring,
 * no database and no HTTP.
 */
final class InMemoryPorts {

    private InMemoryPorts() {}

    static final class Ids implements IdGenerator {
        @Override
        public UUID next() {
            return UUID.randomUUID();
        }
    }

    static final class Budgets implements BudgetPort {
        final Map<BudgetId, Budget> stored = new LinkedHashMap<>();

        @Override
        public void save(Budget budget) {
            stored.put(budget.budgetId(), budget);
        }

        @Override
        public Optional<Budget> load(BudgetId budgetId) {
            return Optional.ofNullable(stored.get(budgetId));
        }

        @Override
        public List<Budget> findByOwner(UserId ownerId) {
            return stored.values().stream().filter(budget -> budget.ownerId().equals(ownerId)).toList();
        }
    }

    static final class Labels implements LabelPort {
        final Map<LabelId, Label> stored = new LinkedHashMap<>();

        @Override
        public void save(Label label) {
            stored.put(label.labelId(), label);
        }

        @Override
        public List<Label> findByUser(UserId userId) {
            return stored.values().stream().filter(label -> label.userId().equals(userId)).toList();
        }

        @Override
        public boolean exists(LabelId labelId) {
            return stored.containsKey(labelId);
        }
    }

    static final class Transactions implements TransactionPort {
        final List<Transaction> stored = new ArrayList<>();

        @Override
        public void save(Transaction transaction) {
            stored.add(transaction);
        }

        @Override
        public List<Transaction> findByBudget(BudgetId budgetId) {
            return stored.stream().filter(transaction -> budgetOf(transaction).equals(budgetId)).toList();
        }

        @Override
        public Optional<Transaction> load(TransactionId transactionId) {
            return stored.stream().filter(t -> t.transactionId().equals(transactionId)).findFirst();
        }

        private BudgetId budgetOf(Transaction transaction) {
            return switch (transaction) {
                case Expense expense -> expense.budgetId();
                case Income income -> income.budgetId();
                case Refund refund -> load(refund.expenseId())
                        .map(this::budgetOf)
                        .orElseThrow();
            };
        }
    }

    static final class Users implements UserPort {
        final Map<String, User> stored = new LinkedHashMap<>();

        @Override
        public void save(User user) {
            stored.put(user.email(), user);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(stored.get(email));
        }
    }

    /** Reversible on purpose: a test wants to assert behaviour, not bcrypt. */
    static final class PlainHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return hash(rawPassword).equals(passwordHash);
        }
    }

    static final class Tokens implements TokenIssuer {
        @Override
        public String issue(UserId userId) {
            return "token:" + userId.id();
        }
    }
}
