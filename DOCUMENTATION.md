# Expense Tracker

A personal web application to plan and follow a budget over a period of time.
The goal is twofold: build a useful budgeting tool, and learn hexagonal
architecture in Java plus IBM i (AS/400) integration.

The core question the app answers: how much room do I have left, globally and
per category, before the end of this period?

## Glossary

| Term | Meaning |
| --- | --- |
| User | Owner of budgets. Authenticated. |
| Budget | A plan attached to a period, with a global total amount. |
| Period | The time window of a budget: a start date and an end date. |
| Transaction | A dated movement of money. An Expense, an Income or a Refund. |
| Income | Money entering the budget. Raises the available amount. |
| Expense | Money leaving the budget. Consumes the available amount. |
| Refund | Money coming back on a specific expense. Reduces what that expense cost. |
| Label | A category of spending (food, rent, transport). Can have a parent label. |
| Envelope | The planned limit for a label inside a budget. One envelope per (budget, label). |
| Money | An amount, stored in minor units. |

## Scope

### Plan a budget
- Create a budget for a period defined by a start date and an end date.
- Set the global total amount of money available for that budget.
- Set a planned limit per label (an envelope), for example food 500.
- The sum of envelope limits is allowed to differ from the budget total. Show
  the gap instead of forbidding it.

### Declare money
- Add an income. The available amount of the budget increases.
- Add an expense. The consumed amount increases, globally and for its label.
- Add a refund on an existing expense. What that expense cost goes down.

### Dashboard
- Global budget total with consumed and remaining amounts.
- One visual per label showing consumed against the envelope limit.
- Drill down from a label to its sub labels and individual expenses.
- Visuals react immediately when a transaction is added.

## Domain model

Everything under `domain` is a Java record with validation in its compact
constructor. There is no framework annotation in the domain.

### Identifiers
`UserId`, `BudgetId`, `LabelId`, `EnvelopeId` and `TransactionId` each wrap a
non null `UUID`. They are distinct types so an id of one kind cannot be passed
where another kind is expected.

### Value objects

**`Money(long minorUnits)`**
An amount held in minor units (cents), so no floating point rounding. The
constructor rejects any value that is not strictly positive. Currency is
deliberately absent: the domain does not carry currency logic.

**`Period(LocalDate startDate, LocalDate endDate)`**
Both dates are required and `startDate` must not be after `endDate`.

### Entities

**`User(UserId userId, String firstname, String lastname, String email)`**
All fields required.

**`Budget(BudgetId budgetId, UserId ownerId, Period period, Money total)`**
A budget belongs to one user, covers one period and carries the global total.

**`Label(LabelId labelId, Optional<LabelId> parentId, String name)`**
A spending category. `parentId` is an explicit `Optional`, empty for a top
level label and set for a sub label. The name cannot be blank and a label
cannot be its own parent. Labels are global, not attached to a budget.

**`Envelope(EnvelopeId envelopeId, BudgetId budgetId, LabelId labelId, Money limit)`**
The join between a budget and a label, carrying the planned limit. This is what
makes a label limit specific to one budget.

### Transactions

`Transaction` is a sealed interface permitting exactly `Expense`, `Income` and
`Refund`. It exposes what all three share: `transactionId()`, `userId()`,
`amount()`, `description()` and `date()`. Being sealed, an exhaustive switch
over the three kinds needs no default branch.

**`Income(TransactionId, BudgetId, UserId, Money amount, String source, String description, LocalDate date)`**
Money entering a budget. `source` records where it came from.

**`Expense(TransactionId, BudgetId, UserId, LabelId, Money amount, String description, LocalDate date)`**
Money leaving a budget, always attached to a label so it can be counted against
an envelope.

**`Refund(TransactionId, TransactionId expenseId, UserId, Money amount, String description, LocalDate date)`**
Money coming back on one specific expense. It points at the refunded expense
through `expenseId` and a refund cannot point at itself. It carries no
`BudgetId`: the budget is the one of the expense it refunds.

### Derived values
Consumed, remaining and available amounts are not stored on any record. They
are computed from the transactions of a budget against the budget total and the
envelope limits.

## Architecture

Hexagonal, three layers under `benoit.kevin.expensetracker`:

```
domain/          budget, envelope, label, money, period, transaction, user
application/     port/in, port/out, service
infrastructure/  adapter/in/web, adapter/out/persistence, adapter/out/ibmi, config
```

The dependency rule points inward. `domain` depends on nothing outside itself.
`application` depends on `domain` and defines the ports. `infrastructure`
depends on `application` and implements the adapters. Nothing in `domain` or
`application` refers to Spring, JPA or HTTP.

Driving ports live in `application/port/in` and are the use cases the web
adapter calls. Driven ports live in `application/port/out` and are the
interfaces the persistence and IBM i adapters implement.

Current state: the domain records exist, the application and infrastructure
packages are created and still empty.

## IBM i (AS/400) integration

A learning objective, kept behind a driven port and optional. The domain never
knows it exists.

- Port: an outgoing interface in `application/port/out`, for example
  `AccountingLedgerGateway`, expressed in domain terms.
- Adapter: `infrastructure/adapter/out/ibmi`, using JT400 with JDBC over Db2
  for i, or program calls to RPG or CL programs.
- First use case: mirror validated expenses into a Db2 for i table, then read a
  consolidated report back.
- A no-op or in-memory implementation of the port lets the app run without an
  IBM i host.

## Technical stack

- Java 25
- Spring Boot 4.1.0: Web MVC, Data JPA, Security, Validation
- PostgreSQL at runtime
- JJWT 0.12.6 for JWT authentication
- Maven, with the Maven wrapper included
- Tests: Spring Boot JPA and Web MVC test starters, Spring Security test

Build and run:

```
./mvnw test
./mvnw spring-boot:run
```
