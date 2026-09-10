package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.BudgetSummary;
import benoit.kevin.expensetracker.application.port.in.BudgetUseCase;
import benoit.kevin.expensetracker.application.port.in.NotOwnerException;
import benoit.kevin.expensetracker.application.port.out.BudgetPort;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.application.port.out.TransactionPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.envelope.Envelope;
import benoit.kevin.expensetracker.domain.envelope.EnvelopeId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.transaction.Expense;
import benoit.kevin.expensetracker.domain.transaction.Income;
import benoit.kevin.expensetracker.domain.transaction.Refund;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BudgetService implements BudgetUseCase {

    private final BudgetPort budgetPort;
    private final LabelPort labelPort;
    private final TransactionPort transactionPort;
    private final IdGenerator idGenerator;

    public BudgetService(BudgetPort budgetPort,
                         LabelPort labelPort,
                         TransactionPort transactionPort,
                         IdGenerator idGenerator) {
        this.budgetPort = Objects.requireNonNull(budgetPort);
        this.labelPort = Objects.requireNonNull(labelPort);
        this.transactionPort = Objects.requireNonNull(transactionPort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public BudgetId createBudget(CreateBudget command) {
        var budget = new Budget(new BudgetId(idGenerator.next()),
                command.owner(),
                command.period(),
                Map.of());
        budgetPort.save(budget);
        return budget.budgetId();
    }

    @Override
    public Budget getBudget(BudgetId budgetId, UserId caller) {
        return loadOwned(budgetId, caller);
    }

    @Override
    public List<Budget> listBudgets(UserId owner) {
        return budgetPort.findByOwner(owner);
    }

    @Override
    public void allocateEnvelope(AllocateEnvelope command) {
        var budget = loadOwned(command.budgetId(), command.caller());
        if (!labelPort.exists(command.labelId())) {
            throw new IllegalArgumentException("unknown label");
        }
        var envelope = new Envelope(new EnvelopeId(idGenerator.next()), command.limit());
        budgetPort.save(budget.withEnvelope(command.labelId(), envelope));
    }

    @Override
    public BudgetSummary getSummary(BudgetId budgetId, UserId caller) {
        var budget = loadOwned(budgetId, caller);
        var transactions = transactionPort.findByBudget(budgetId);

        var expenseLabels = new HashMap<TransactionId, LabelId>();
        var consumedByLabel = new HashMap<LabelId, Long>();
        long totalIncome = 0;

        // Expenses first: a refund can appear before the expense it points at.
        for (var transaction : transactions) {
            switch (transaction) {
                case Expense expense -> {
                    expenseLabels.put(expense.transactionId(), expense.labelId());
                    consumedByLabel.merge(expense.labelId(), expense.amount().minorUnits(), Long::sum);
                }
                case Income income -> totalIncome += income.amount().minorUnits();
                case Refund ignored -> { }
            }
        }
        for (var transaction : transactions) {
            if (transaction instanceof Refund refund) {
                var labelId = expenseLabels.get(refund.expenseId());
                if (labelId != null) {
                    consumedByLabel.merge(labelId, -refund.amount().minorUnits(), Long::sum);
                }
            }
        }

        var lines = new ArrayList<BudgetSummary.LabelLine>();
        var labelIds = new java.util.LinkedHashSet<LabelId>(budget.envelopes().keySet());
        labelIds.addAll(consumedByLabel.keySet());

        long totalConsumed = 0;
        for (var labelId : labelIds) {
            var envelope = budget.envelopes().get(labelId);
            long limit = envelope == null ? 0 : envelope.limit().minorUnits();
            long consumed = consumedByLabel.getOrDefault(labelId, 0L);
            totalConsumed += consumed;
            lines.add(new BudgetSummary.LabelLine(labelId, limit, consumed, limit - consumed));
        }

        return new BudgetSummary(budgetId,
                budget.plannedTotal().minorUnits(),
                totalIncome,
                totalConsumed,
                List.copyOf(lines));
    }

    private Budget loadOwned(BudgetId budgetId, UserId caller) {
        var budget = budgetPort.load(budgetId)
                .orElseThrow(() -> new NoSuchElementException("budget not found"));
        if (!budget.ownerId().equals(caller)) {
            throw new NotOwnerException();
        }
        return budget;
    }
}
