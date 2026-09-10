package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.application.port.in.TransactionUseCase;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.transaction.Expense;
import benoit.kevin.expensetracker.domain.transaction.Income;
import benoit.kevin.expensetracker.domain.transaction.Refund;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
public class TransactionController {

    public record ExpenseRequest(@NotNull UUID labelId, @Positive long amountMinorUnits,
                                 @NotBlank String description, @NotNull LocalDate date) {}

    public record IncomeRequest(@Positive long amountMinorUnits, @NotBlank String source,
                                @NotBlank String description, @NotNull LocalDate date) {}

    public record RefundRequest(@Positive long amountMinorUnits, @NotBlank String description,
                                @NotNull LocalDate date) {}

    public record TransactionResponse(UUID id, String type, long amountMinorUnits, String description,
                                      LocalDate date, UUID labelId, UUID expenseId, String source) {
        static TransactionResponse of(Transaction transaction) {
            return switch (transaction) {
                case Expense expense -> new TransactionResponse(expense.transactionId().id(), "EXPENSE",
                        expense.amount().minorUnits(), expense.description(), expense.date(),
                        expense.labelId().id(), null, null);
                case Income income -> new TransactionResponse(income.transactionId().id(), "INCOME",
                        income.amount().minorUnits(), income.description(), income.date(),
                        null, null, income.source());
                case Refund refund -> new TransactionResponse(refund.transactionId().id(), "REFUND",
                        refund.amount().minorUnits(), refund.description(), refund.date(),
                        null, refund.expenseId().id(), null);
            };
        }
    }

    private final TransactionUseCase transactionUseCase;

    public TransactionController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = Objects.requireNonNull(transactionUseCase);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse addExpense(@PathVariable UUID budgetId,
                                          @Valid @RequestBody ExpenseRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        var id = transactionUseCase.recordExpense(new TransactionUseCase.RecordExpense(
                new BudgetId(budgetId), new LabelId(request.labelId()), new Money(request.amountMinorUnits()),
                request.description(), request.date(), CurrentUser.of(jwt)));
        return new TransactionResponse(id.id(), "EXPENSE", request.amountMinorUnits(),
                request.description(), request.date(), request.labelId(), null, null);
    }

    @PostMapping("/budgets/{budgetId}/incomes")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse addIncome(@PathVariable UUID budgetId,
                                         @Valid @RequestBody IncomeRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        var id = transactionUseCase.recordIncome(new TransactionUseCase.RecordIncome(
                new BudgetId(budgetId), new Money(request.amountMinorUnits()), request.source(),
                request.description(), request.date(), CurrentUser.of(jwt)));
        return new TransactionResponse(id.id(), "INCOME", request.amountMinorUnits(),
                request.description(), request.date(), null, null, request.source());
    }

    @PostMapping("/transactions/{expenseId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse addRefund(@PathVariable UUID expenseId,
                                         @Valid @RequestBody RefundRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        var id = transactionUseCase.recordRefund(new TransactionUseCase.RecordRefund(
                new TransactionId(expenseId), new Money(request.amountMinorUnits()),
                request.description(), request.date(), CurrentUser.of(jwt)));
        return new TransactionResponse(id.id(), "REFUND", request.amountMinorUnits(),
                request.description(), request.date(), null, expenseId, null);
    }

    @GetMapping("/budgets/{budgetId}/transactions")
    public List<TransactionResponse> list(@PathVariable UUID budgetId, @AuthenticationPrincipal Jwt jwt) {
        return transactionUseCase.listByBudget(new BudgetId(budgetId), CurrentUser.of(jwt)).stream()
                .map(TransactionResponse::of)
                .toList();
    }
}
