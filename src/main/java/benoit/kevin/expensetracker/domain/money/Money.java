package benoit.kevin.expensetracker.domain.money;

/**
 * the domain should not carry Currency logic
 */
public record Money(long minorUnits) {

    public static final Money ZERO = new Money(0);

    public Money {
        if (minorUnits < 0) {
            throw new IllegalArgumentException("minorUnits must not be negative");
        }
    }

    public Money plus(Money other) {
        return new Money(minorUnits + other.minorUnits);
    }

    public Money minus(Money other) {
        return new Money(minorUnits - other.minorUnits);
    }
}
