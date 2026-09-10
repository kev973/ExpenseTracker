package benoit.kevin.expensetracker.application.port.out;

import benoit.kevin.expensetracker.domain.user.UserId;

public interface TokenIssuer {
    String issue(UserId userId);
}
