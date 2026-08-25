package benoit.kevin.expensetracker.domain.label;

import java.util.Objects;
import java.util.Optional;

public record Label(LabelId labelId, Optional<LabelId> parentId, String name) {
    public Label{
        Objects.requireNonNull(labelId);
        Objects.requireNonNull(parentId);
        Objects.requireNonNull(name);
        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }
        if(parentId.isPresent() && parentId.get().equals(labelId)){
            throw new IllegalArgumentException("parentId cannot be the same as labelId");
        }
    }
}
