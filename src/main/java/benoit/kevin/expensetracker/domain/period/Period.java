package benoit.kevin.expensetracker.domain.period;

import java.time.LocalDate;
import java.util.Objects;

public record Period(LocalDate startDate, LocalDate endDate) {
    public Period{
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);

        if(startDate.isAfter(endDate)){
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }
}
