package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.budget.CreateBudgetCommand;
import benoit.kevin.expensetracker.application.port.in.budget.CreateBudgetUseCase;
import benoit.kevin.expensetracker.application.port.out.budget.BudgetIdGenerator;
import benoit.kevin.expensetracker.application.port.out.budget.SaveBudgetPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;

import java.util.Map;
import java.util.Objects;

public class CreateBudgetService implements CreateBudgetUseCase {

    private final SaveBudgetPort saveBudgetPort;
    private final BudgetIdGenerator budgetIdGenerator;

    public CreateBudgetService(SaveBudgetPort saveBudgetPort,
                               BudgetIdGenerator budgetIdGenerator) {
        this.saveBudgetPort = Objects.requireNonNull(saveBudgetPort);
        this.budgetIdGenerator = Objects.requireNonNull(budgetIdGenerator);
    }

    @Override
    public BudgetId createBudget(CreateBudgetCommand command) {
        var budgetId = budgetIdGenerator.next();
        var budget = new Budget(budgetId, command.owner(), command.period());
        saveBudgetPort.save(budget);
        return budget.budgetId();
    }
}
