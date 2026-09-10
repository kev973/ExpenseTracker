package benoit.kevin.expensetracker.application.port.out;

import java.util.UUID;

public interface IdGenerator {
    UUID next();
}
