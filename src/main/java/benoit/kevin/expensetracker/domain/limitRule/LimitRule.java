package benoit.kevin.expensetracker.domain.limitRule;

import benoit.kevin.expensetracker.domain.label.LabelId;

public record LimitRule(LimitRuleId id, LabelId labelId, long total) {
}
