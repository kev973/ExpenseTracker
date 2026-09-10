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

**`User(UserId userId, String firstname, String lastname, String email, String passwordHash)`**
All fields required; email and `passwordHash` cannot be blank. The record holds
the hash and never computes one — hashing lives behind the `PasswordHasher`
port.

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
infrastructure/  adapter/in/web, adapter/out/persistence, adapter/out/security,
                 adapter/out/id, adapter/out/ibmi, config
```

The dependency rule points inward. `domain` depends on nothing outside itself.
`application` depends on `domain` and defines the ports. `infrastructure`
depends on `application` and implements the adapters. Nothing in `domain` or
`application` refers to Spring, JPA or HTTP.

Driving ports live in `application/port/in` and are the use cases the web
adapter calls. Driven ports live in `application/port/out` and are the
interfaces the persistence and IBM i adapters implement.

Services are plain constructor injected classes with no Spring annotation. They
are wired by an explicit `@Bean` method per use case in
`infrastructure/config/BeanConfiguration`,
which is what keeps the application layer framework free. Those `@Bean` methods
do not null check their parameters: Spring never injects null, and a missing
dependency fails the context at startup. Validation belongs to the class that
owns the invariant, not to the wiring.

### Current state

Every use case below is implemented end to end and exercised by the test page.

- **Authentication.** `AuthUseCase` / `AuthService`: self-serve registration and
  login, returning a 24h HS256 JWT whose subject is the `UserId`. Password
  hashing sits behind `PasswordHasher` and token signing behind `TokenIssuer`,
  so the application layer knows *that* a password is checked, never *how*.
  Unknown email and wrong password fail with the same message, so the endpoint
  does not reveal which addresses are registered.
- **Budgets.** Create, read, list, and allocate an envelope per label.
- **Labels.** Create (optionally under a parent) and list.
- **Transactions.** Record an expense, an income and a refund; list a budget's
  transactions. A refund must point at an `Expense` and cannot exceed it.
- **Summary.** `BudgetSummary` is computed in `BudgetService` from the loaded
  aggregate plus the budget's transactions: planned total, income, consumed, and
  one line per label with limit, consumed and remaining. Its amounts are plain
  `long` minor units rather than `Money`, because remaining must be able to go
  negative and `Money` forbids that by design.

**Ownership is enforced in the services, not the controllers**, so a new
endpoint cannot forget it. Every use case taking a `BudgetId` also takes the
caller's `UserId`, compares it with `Budget.ownerId` and raises
`NotOwnerException`, which the web layer turns into a 403. No request body or
query string ever carries an owner id: it always comes from the token subject,
read through the `CurrentUser` helper.

**One use case interface and one service per aggregate.** Strict hexagonal would
give each operation its own interface, command record and service — roughly 24
files for the operations above, against 7 here. Commands are nested records
inside their use case interface for the same reason. The trade-off is that a
service has more than one reason to change; if one grows past about four
methods, split it then.

`IdGenerator` is a single out port returning a `UUID`, rather than one generator
port per aggregate.

Not built yet:
- Nothing for the IBM i integration beyond an empty package.
- No refresh tokens, logout or revocation: a token is valid for 24 hours, full stop.
- Labels cannot be renamed or deleted, and nothing checks that a budget's
  envelopes still point at labels that exist.
- No pagination anywhere.
- Test coverage is deliberately thin: three service tests pinning the envelope,
  refund and credential rules, with in-memory fakes of the ports and no Spring
  context. There is no test of the web or persistence adapters.

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
- Spring Boot 4.1.0: Web MVC, Data JPA, Security, Validation, OAuth2 Resource Server
- PostgreSQL at runtime, started by `compose.yaml` and picked up by
  `spring-boot-docker-compose`. The container publishes **5433** on the host,
  because a locally installed Postgres already owns 5432.
- JWT via Spring Security's own `JwtEncoder`/`JwtDecoder` (Nimbus), so there is
  no third-party JWT library and no hand-written filter. The HS256 secret comes
  from the `JWT_SECRET` environment variable, with a local-only default.
- Maven, with the Maven wrapper included
- Tests: Spring Boot JPA and Web MVC test starters, Spring Security test

Build and run:

```
./mvnw test
./mvnw spring-boot:run
```

## Frontend

`frontend/` is a Vite + React 19 + TypeScript app. `npm run build` compiles it
into `src/main/resources/static/`, so `./mvnw spring-boot:run` alone serves the
UI; the built output is committed for that reason, and is regenerated rather
than edited. While working on it, `npm run dev` proxies `/auth`, `/budgets`,
`/labels` and `/transactions` to `localhost:8080`.

```
cd frontend
npm install
npm run dev      # development, with the API proxied
npm run build    # regenerate what Spring serves
```

The token returned by register or login goes into `sessionStorage`; `src/api.ts`
is the single place that attaches `Authorization: Bearer`, converts minor units,
and turns a 401 into a signed-out state. No component does arithmetic on amounts.

Animation uses `motion` (motion.dev): bars spring up on load, tiles and the
error banner fade in. Everything is disabled under
`prefers-reduced-motion: reduce`.

### The chart

One vertical bar per label — consumed against the planned limit, which is the
question the app exists to answer.

- **The limit is a target, not a series**, so it is always drawn as a dashed rule
  across the bar rather than relying on a second fill being told apart from the
  first. That matters most in dark mode, where the soft track and the bar sit
  close together.
- Colours come from a validated palette: series blue for consumed, status red
  for an overspent envelope. Both were checked against the light and dark
  surfaces with the data-viz palette validator (lightness band, chroma floor,
  colour-blind separation, normal-vision separation, contrast).
- **Overspend never relies on colour alone** — an overspent bar also carries a
  `⚠ N over` direct label, and the legend gains an explicit entry.
- Every bar carries its consumed value as a direct label, has a hover tooltip
  with limit / consumed / remaining, and a hit target wider than the bar itself.
- The transactions table below is the table view of the same data, and scrolls
  inside its own container so the page never scrolls sideways.

Dark mode is a selected palette, not an inverted one: every colour is defined on
bare `:root` first, then re-declared under both `prefers-color-scheme: dark` and
`[data-theme="dark"]`.
