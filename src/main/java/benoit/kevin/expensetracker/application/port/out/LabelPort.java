package benoit.kevin.expensetracker.application.port.out;

import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;

public interface LabelPort {
    void save(Label label);

    List<Label> findByUser(UserId userId);

    boolean exists(LabelId labelId);
}
