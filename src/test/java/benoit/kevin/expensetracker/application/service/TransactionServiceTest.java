package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.BudgetUseCase;
import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.application.port.in.TransactionUseCase;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {

    private final UserId owner = new UserId(UUID.randomUUID());
    private final LocalDate day = LocalDate.of(2026, 1, 10);

    private BudgetService budgetService;
    private TransactionService transactionService;
    private BudgetId budgetId;
    private LabelId food;

    @BeforeEach
    void setUp() {
        var ids = new InMemoryPorts.Ids();
        var budgets = new InMemoryPorts.Budgets();
        var labels = new InMemoryPorts.Labels();
        var transactions = new InMemoryPorts.Transactions();

        budgetService = new BudgetService(budgets, labels, transactions, ids);
        transactionService = new TransactionService(transactions, budgets, labels, ids);
        var labelService = new LabelService(labels, ids);

        budgetId = budgetService.createBudget(new BudgetUseCase.CreateBudget(owner,
                new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));
        food = labelService.createLabel(new LabelUseCase.CreateLabel(owner, "food", Optional.empty()));
        budgetService.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(budgetId, food, new Money(50_000), owner));
    }

    @Test
    void aRefundReducesWhatItsExpenseCost() {
        var expenseId = transactionService.recordExpense(new TransactionUseCase.RecordExpense(
                budgetId, food, new Money(2_000), "groceries", day, owner));
        transactionService.recordRefund(new TransactionUseCase.RecordRefund(
                expenseId, new Money(500), "returned an item", day, owner));

        var summary = budgetService.getSummary(budgetId, owner);
        var line = summary.lines().getFirst();
        assertEquals(1_500, line.consumed());
        assertEquals(48_500, line.remaining());
        assertEquals(1_500, summary.totalConsumed());
    }

    @Test
    void aRefundCannotPointAtAnIncome() {
        var incomeId = transactionService.recordIncome(new TransactionUseCase.RecordIncome(
                budgetId, new Money(100_000), "salary", "january", day, owner));

        assertThrows(IllegalArgumentException.class, () -> transactionService.recordRefund(
                new TransactionUseCase.RecordRefund(incomeId, new Money(1), "nope", day, owner)));
    }

    @Test
    void aRefundCannotExceedItsExpense() {
        var expenseId = transactionService.recordExpense(new TransactionUseCase.RecordExpense(
                budgetId, food, new Money(2_000), "groceries", day, owner));

        assertThrows(IllegalArgumentException.class, () -> transactionService.recordRefund(
                new TransactionUseCase.RecordRefund(expenseId, new Money(2_001), "too much", day, owner)));
    }

    @Test
    void incomeIsCountedButNotConsumed() {
        transactionService.recordIncome(new TransactionUseCase.RecordIncome(
                budgetId, new Money(100_000), "salary", "january", day, owner));

        var summary = budgetService.getSummary(budgetId, owner);
        assertEquals(100_000, summary.totalIncome());
        assertEquals(0, summary.totalConsumed());
    }
}
