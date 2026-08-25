package benoit.kevin.expensetracker.domain.money;

/**
 * the domain should not carry Currency logic
 */
public record Money(long minorUnits) {
    public Money {
        if (minorUnits <= 0) {
            throw new IllegalArgumentException("minorUnits must be positive");
        }
    }
}
