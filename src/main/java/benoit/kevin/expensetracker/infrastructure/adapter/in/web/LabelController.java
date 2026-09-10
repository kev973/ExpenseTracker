package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/labels")
public class LabelController {

    public record CreateLabelRequest(@NotBlank String name, UUID parentId) {}

    public record LabelResponse(UUID id, String name, UUID parentId) {
        static LabelResponse of(Label label) {
            return new LabelResponse(label.labelId().id(), label.name(),
                    label.parentId().map(LabelId::id).orElse(null));
        }
    }

    private final LabelUseCase labelUseCase;

    public LabelController(LabelUseCase labelUseCase) {
        this.labelUseCase = Objects.requireNonNull(labelUseCase);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse create(@Valid @RequestBody CreateLabelRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        var labelId = labelUseCase.createLabel(new LabelUseCase.CreateLabel(
                CurrentUser.of(jwt),
                request.name(),
                Optional.ofNullable(request.parentId()).map(LabelId::new)));
        return new LabelResponse(labelId.id(), request.name(), request.parentId());
    }

    @GetMapping
    public List<LabelResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return labelUseCase.listLabels(CurrentUser.of(jwt)).stream().map(LabelResponse::of).toList();
    }
}
