package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.application.port.in.AuthUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public record RegisterRequest(@NotBlank String firstname,
                                  @NotBlank String lastname,
                                  @NotBlank @Email String email,
                                  @NotBlank String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record TokenResponse(String token) {}

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = Objects.requireNonNull(authUseCase);
    }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return new TokenResponse(authUseCase.register(new AuthUseCase.Register(
                request.firstname(), request.lastname(), request.email(), request.password())));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return new TokenResponse(authUseCase.login(new AuthUseCase.Login(request.email(), request.password())));
    }
}
