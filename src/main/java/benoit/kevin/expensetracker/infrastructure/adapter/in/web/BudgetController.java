package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.application.port.in.budget.CreateBudgetCommand;
import benoit.kevin.expensetracker.application.port.in.budget.CreateBudgetUseCase;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/budgets")
public class BudgetController {
    private final CreateBudgetUseCase createBudgetUseCase;

    public BudgetController(CreateBudgetUseCase createBudgetUseCase) {
        this.createBudgetUseCase = Objects.requireNonNull(createBudgetUseCase);
    }


    @PostMapping
    public ResponseEntity<BudgetId> createBudget(@RequestBody CreateBudgetRequest request) {
        var command = new CreateBudgetCommand(new UserId(request.ownerId()), new Period(request.startDate(), request.endDate()));
        var budgetId = createBudgetUseCase.createBudget(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetId);
    }

}
