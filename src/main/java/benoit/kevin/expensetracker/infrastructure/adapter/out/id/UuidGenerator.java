package benoit.kevin.expensetracker.infrastructure.adapter.out.id;

import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidGenerator implements IdGenerator {

    @Override
    public UUID next() {
        return UUID.randomUUID();
    }
}
