package benoit.kevin.expensetracker.infrastructure.config;

import benoit.kevin.expensetracker.application.port.in.budget.CreateBudgetUseCase;
import benoit.kevin.expensetracker.application.port.out.budget.BudgetIdGenerator;
import benoit.kevin.expensetracker.application.port.out.budget.SaveBudgetPort;
import benoit.kevin.expensetracker.application.service.CreateBudgetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BudgetConfiguration {

    @Bean
    public CreateBudgetUseCase createBudgetUseCase(SaveBudgetPort saveBudgetPort, BudgetIdGenerator budgetIdGenerator) {
        return new CreateBudgetService(saveBudgetPort, budgetIdGenerator);
    }
}
