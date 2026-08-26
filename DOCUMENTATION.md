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
| Budget | A plan attached to a period, holding one envelope per label. |
| Period | The time window of a budget: a start date and an end date. |
| Transaction | A dated movement of money. An Expense, an Income or a Refund. |
| Income | Money entering the budget. Raises the available amount. |
| Expense | Money leaving the budget. Consumes the available amount. |
| Refund | Money coming back on a specific expense. Reduces what that expense cost. |
| Label | A category of spending (food, rent, transport). Can have a parent label. |
| Envelope | The planned limit for a label inside a budget. One envelope per (budget, label). |
| Planned total | The sum of a budget's envelope limits. Derived, never stored. |
| Money | An amount, stored in minor units. |

## Scope

### Plan a budget
- Create a budget for a period defined by a start date and an end date. A new
  budget starts empty, with no envelopes.
- Set a planned limit per label (an envelope), for example food 500.
- A budget has no limit of its own. Its planned total is the sum of its
  envelope limits, so allocating more is always allowed and never rejected.
- Compare the planned total against the income declared on the budget and show
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
constructor rejects a negative value, so zero is a valid amount. Currency is
deliberately absent: the domain does not carry currency logic.

**`Period(LocalDate startDate, LocalDate endDate)`**
Both dates are required and `startDate` must not be after `endDate`.

### Entities

**`User(UserId userId, String firstname, String lastname, String email)`**
All fields required.

**`Budget(BudgetId budgetId, UserId ownerId, Period period, Map<LabelId, Envelope> envelopes)`**
A budget belongs to one user, covers one period and owns its envelopes. It is
the aggregate root: an envelope is only ever reached through the budget that
holds it.

Keying the map by `LabelId` makes "one envelope per label" true by
construction, with no check to run and nothing to reject. It also keeps the
reference to the other aggregate as an id, so a renamed label cannot leave a
stale copy of itself inside a budget.

Two behaviours, both pure:
- `plannedTotal()` folds the envelope limits into a `Money`. The budget has no
  total of its own, so there is nothing that can drift out of step with the
  envelopes that produce it.
- `withEnvelope(LabelId, Envelope)` returns a new budget with that envelope
  added. Allocating again to a label that already has one replaces it.

**`Label(LabelId labelId, Optional<LabelId> parentId, UserId userId, String name)`**
A spending category, owned by one user. `parentId` is an explicit `Optional`,
empty for a top level label and set for a sub label. The name cannot be blank
and a label cannot be its own parent. A label is its own aggregate, not
attached to a budget, and lives independently of the budgets that allocate to
it.

**`Envelope(EnvelopeId envelopeId, Money limit)`**
The planned limit for one label inside one budget. It holds neither a
`BudgetId` nor a `LabelId`: containment in `Budget.envelopes` carries the first
and the map key carries the second, and a child repeating what its parent
already states is a second source of truth that can disagree.

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
No aggregate stores a figure it can compute. The planned total comes from the
envelope limits, and the consumed, remaining and available amounts come from
the transactions of a budget measured against those limits. A stored copy of a
derived value is a copy that can go stale.

Figures a screen needs but no aggregate owns, such as a label's name next to
its envelope, belong to the read model. They are joined in the query use case
or the web response record, never by putting label data inside a budget.

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

Services are plain constructor injected classes with no Spring annotation. They
are wired by an explicit `@Bean` method per use case in `infrastructure/config`,
which is what keeps the application layer framework free. Those `@Bean` methods
do not null check their parameters: Spring never injects null, and a missing
dependency fails the context at startup. Validation belongs to the class that
owns the invariant, not to the wiring.

### Current state

Implemented, one vertical slice end to end:
- Create a budget: `CreateBudgetUseCase` and `CreateBudgetCommand`, served by
  `CreateBudgetService`, driven by `POST /budgets` through `BudgetController`,
  persisted through `SaveBudgetPort` and `BudgetPersistenceAdapter`. Ids come
  from `BudgetIdGenerator`, implemented by `UuidBudgetIdGenerator`.
- Persistence mapping for the whole budget aggregate: `BudgetJpaEntity` owns
  `EnvelopeJpaEntity` through a `@OneToMany` with cascade and orphan removal.
  Both keep their JPA concerns inside the adapter package and convert with
  `fromDomain` and `toDomain`.

Not built yet:
- No way to read a budget back. `LoadBudgetPort` is missing, so
  `BudgetJpaEntity.toDomain` currently has no caller.
- No `AddEnvelopeUseCase`, so envelopes cannot be created through the API. It
  needs an `EnvelopeIdGenerator` out port mirroring `BudgetIdGenerator`.
- Nothing for labels: no persistence, no use case, no endpoint. An envelope
  needs a valid `LabelId`, and no referential check exists for it.
- Nothing for transactions, users or authentication.
- No error handling at the boundary. A rejected invariant surfaces as HTTP 500,
  and `CreateBudgetRequest` carries no bean validation.
- `src/test` is empty.

An absent budget travels as an `Optional` from the port through the use case,
and the controller is the only place that turns it into a 404. There is no
not found exception type.

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

- Java 25. The build fails with `release version 25 not supported` unless
  `JAVA_HOME` points at a JDK 25, which is not the default on every machine:
  `JAVA_HOME=$(/usr/libexec/java_home -v 25) ./mvnw compile` on macOS.
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

## Frontend

`frontend/` holds a Vite scaffold: React 19, TypeScript, oxlint for linting.
Still the generated starter, with no router, no API client and no screen of the
app yet.

```
cd frontend
npm install
npm run dev
```

Planned first screens: the budget list, a create budget form, and a budget
detail page showing the planned total with one row per envelope and an add
envelope form. The minor units conversion belongs in a single place in the API
client, so no component does arithmetic on amounts.
