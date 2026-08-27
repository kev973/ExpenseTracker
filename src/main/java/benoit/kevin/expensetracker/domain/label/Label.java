package benoit.kevin.expensetracker.domain.label;

import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;
import java.util.Optional;

public record Label(String name, Optional<String> parent, UserId userId) {
    public Label{
        Objects.requireNonNull(parent);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(name);
        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }
        if(parent.isPresent() && parent.get().equals(name)){
            throw new IllegalArgumentException("parent cannot be the same as name");
        }
    }
}
