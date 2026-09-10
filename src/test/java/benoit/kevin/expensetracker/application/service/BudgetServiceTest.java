package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.BudgetUseCase;
import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.application.port.in.NotOwnerException;
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

class BudgetServiceTest {

    private final UserId owner = new UserId(UUID.randomUUID());
    private final UserId stranger = new UserId(UUID.randomUUID());
    private final Period period = new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

    private InMemoryPorts.Labels labels;
    private BudgetService budgetService;
    private LabelService labelService;

    @BeforeEach
    void setUp() {
        var ids = new InMemoryPorts.Ids();
        labels = new InMemoryPorts.Labels();
        budgetService = new BudgetService(new InMemoryPorts.Budgets(), labels, new InMemoryPorts.Transactions(), ids);
        labelService = new LabelService(labels, ids);
    }

    private LabelId label(String name) {
        return labelService.createLabel(new LabelUseCase.CreateLabel(owner, name, Optional.empty()));
    }

    @Test
    void plannedTotalSumsEnvelopeLimits() {
        var budgetId = budgetService.createBudget(new BudgetUseCase.CreateBudget(owner, period));
        budgetService.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(budgetId, label("food"), new Money(50_000), owner));
        budgetService.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(budgetId, label("rent"), new Money(90_000), owner));

        assertEquals(140_000, budgetService.getBudget(budgetId, owner).plannedTotal().minorUnits());
    }

    @Test
    void allocatingTwiceForOneLabelReplacesTheEnvelope() {
        var budgetId = budgetService.createBudget(new BudgetUseCase.CreateBudget(owner, period));
        var food = label("food");
        budgetService.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(budgetId, food, new Money(50_000), owner));
        budgetService.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(budgetId, food, new Money(30_000), owner));

        var budget = budgetService.getBudget(budgetId, owner);
        assertEquals(1, budget.envelopes().size());
        assertEquals(30_000, budget.plannedTotal().minorUnits());
    }

    @Test
    void allocatingForAnUnknownLabelIsRejected() {
        var budgetId = budgetService.createBudget(new BudgetUseCase.CreateBudget(owner, period));
        var unknown = new LabelId(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> budgetService.allocateEnvelope(
                new BudgetUseCase.AllocateEnvelope(budgetId, unknown, new Money(1), owner)));
    }

    @Test
    void anotherUserCannotReadTheBudget() {
        var budgetId = budgetService.createBudget(new BudgetUseCase.CreateBudget(owner, period));

        assertThrows(NotOwnerException.class, () -> budgetService.getBudget(budgetId, stranger));
    }
}
