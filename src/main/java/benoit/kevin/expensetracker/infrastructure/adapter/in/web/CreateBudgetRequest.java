package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import java.time.LocalDate;

import java.util.UUID;

public record CreateBudgetRequest(UUID ownerId, LocalDate startDate, LocalDate endDate) {}
