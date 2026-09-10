package benoit.kevin.expensetracker.infrastructure.config;

import benoit.kevin.expensetracker.application.port.in.AuthUseCase;
import benoit.kevin.expensetracker.application.port.in.BudgetUseCase;
import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.application.port.in.TransactionUseCase;
import benoit.kevin.expensetracker.application.port.out.BudgetPort;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.application.port.out.PasswordHasher;
import benoit.kevin.expensetracker.application.port.out.TokenIssuer;
import benoit.kevin.expensetracker.application.port.out.TransactionPort;
import benoit.kevin.expensetracker.application.port.out.UserPort;
import benoit.kevin.expensetracker.application.service.AuthService;
import benoit.kevin.expensetracker.application.service.BudgetService;
import benoit.kevin.expensetracker.application.service.LabelService;
import benoit.kevin.expensetracker.application.service.TransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application layer stays framework free: services carry no annotation and are
 * wired here instead.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public AuthUseCase authUseCase(UserPort userPort, PasswordHasher passwordHasher,
                                   TokenIssuer tokenIssuer, IdGenerator idGenerator) {
        return new AuthService(userPort, passwordHasher, tokenIssuer, idGenerator);
    }

    @Bean
    public LabelUseCase labelUseCase(LabelPort labelPort, IdGenerator idGenerator) {
        return new LabelService(labelPort, idGenerator);
    }

    @Bean
    public BudgetUseCase budgetUseCase(BudgetPort budgetPort, LabelPort labelPort,
                                       TransactionPort transactionPort, IdGenerator idGenerator) {
        return new BudgetService(budgetPort, labelPort, transactionPort, idGenerator);
    }

    @Bean
    public TransactionUseCase transactionUseCase(TransactionPort transactionPort, BudgetPort budgetPort,
                                                 LabelPort labelPort, IdGenerator idGenerator) {
        return new TransactionService(transactionPort, budgetPort, labelPort, idGenerator);
    }
}
