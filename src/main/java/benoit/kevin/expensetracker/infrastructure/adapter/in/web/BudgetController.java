package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.application.port.in.BudgetSummary;
import benoit.kevin.expensetracker.application.port.in.BudgetUseCase;
import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.period.Period;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    public record CreateBudgetRequest(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {}

    public record SetEnvelopeRequest(@PositiveOrZero long limitMinorUnits) {}

    public record EnvelopeResponse(UUID labelId, String labelName, long limitMinorUnits) {}

    public record BudgetResponse(UUID id, LocalDate startDate, LocalDate endDate,
                                 long plannedTotal, List<EnvelopeResponse> envelopes) {}

    public record SummaryLineResponse(UUID labelId, String labelName,
                                      long limitMinorUnits, long consumed, long remaining) {}

    public record SummaryResponse(UUID budgetId, long plannedTotal, long totalIncome,
                                  long totalConsumed, List<SummaryLineResponse> lines) {}

    private final BudgetUseCase budgetUseCase;
    private final LabelUseCase labelUseCase;

    public BudgetController(BudgetUseCase budgetUseCase, LabelUseCase labelUseCase) {
        this.budgetUseCase = Objects.requireNonNull(budgetUseCase);
        this.labelUseCase = Objects.requireNonNull(labelUseCase);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody CreateBudgetRequest request,
                                 @AuthenticationPrincipal Jwt jwt) {
        var budgetId = budgetUseCase.createBudget(new BudgetUseCase.CreateBudget(
                CurrentUser.of(jwt), new Period(request.startDate(), request.endDate())));
        return toResponse(budgetUseCase.getBudget(budgetId, CurrentUser.of(jwt)), Map.of());
    }

    @GetMapping
    public List<BudgetResponse> list(@AuthenticationPrincipal Jwt jwt) {
        var names = labelNames(jwt);
        return budgetUseCase.listBudgets(CurrentUser.of(jwt)).stream()
                .map(budget -> toResponse(budget, names))
                .toList();
    }

    @GetMapping("/{id}")
    public BudgetResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return toResponse(budgetUseCase.getBudget(new BudgetId(id), CurrentUser.of(jwt)), labelNames(jwt));
    }

    @PutMapping("/{id}/envelopes/{labelId}")
    public BudgetResponse setEnvelope(@PathVariable UUID id,
                                      @PathVariable UUID labelId,
                                      @Valid @RequestBody SetEnvelopeRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        budgetUseCase.allocateEnvelope(new BudgetUseCase.AllocateEnvelope(
                new BudgetId(id), new LabelId(labelId), new Money(request.limitMinorUnits()), CurrentUser.of(jwt)));
        return get(id, jwt);
    }

    @GetMapping("/{id}/summary")
    public SummaryResponse summary(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var summary = budgetUseCase.getSummary(new BudgetId(id), CurrentUser.of(jwt));
        var names = labelNames(jwt);
        return new SummaryResponse(summary.budgetId().id(),
                summary.plannedTotal(),
                summary.totalIncome(),
                summary.totalConsumed(),
                summary.lines().stream().map(line -> toResponse(line, names)).toList());
    }

    /**
     * Label names live in their own aggregate, so the read model joins them here rather
     * than storing a copy inside the budget.
     */
    private Map<UUID, String> labelNames(Jwt jwt) {
        return labelUseCase.listLabels(CurrentUser.of(jwt)).stream()
                .collect(java.util.stream.Collectors.toMap(label -> label.labelId().id(),
                        Label::name, (first, second) -> first));
    }

    private static BudgetResponse toResponse(Budget budget, Map<UUID, String> names) {
        var envelopes = budget.envelopes().entrySet().stream()
                .map(entry -> new EnvelopeResponse(entry.getKey().id(),
                        names.get(entry.getKey().id()),
                        entry.getValue().limit().minorUnits()))
                .toList();
        return new BudgetResponse(budget.budgetId().id(),
                budget.period().startDate(),
                budget.period().endDate(),
                budget.plannedTotal().minorUnits(),
                envelopes);
    }

    private static SummaryLineResponse toResponse(BudgetSummary.LabelLine line, Map<UUID, String> names) {
        return new SummaryLineResponse(line.labelId().id(), names.get(line.labelId().id()),
                line.limit(), line.consumed(), line.remaining());
    }
}
