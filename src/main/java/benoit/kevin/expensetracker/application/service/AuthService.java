package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.AuthUseCase;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.PasswordHasher;
import benoit.kevin.expensetracker.application.port.out.TokenIssuer;
import benoit.kevin.expensetracker.application.port.out.UserPort;
import benoit.kevin.expensetracker.domain.user.User;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;

public class AuthService implements AuthUseCase {

    private final UserPort userPort;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final IdGenerator idGenerator;

    public AuthService(UserPort userPort,
                       PasswordHasher passwordHasher,
                       TokenIssuer tokenIssuer,
                       IdGenerator idGenerator) {
        this.userPort = Objects.requireNonNull(userPort);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public String register(Register command) {
        if (userPort.findByEmail(command.email()).isPresent()) {
            throw new IllegalArgumentException("email already registered");
        }
        var user = new User(new UserId(idGenerator.next()),
                command.firstname(),
                command.lastname(),
                command.email(),
                passwordHasher.hash(command.rawPassword()));
        userPort.save(user);
        return tokenIssuer.issue(user.userId());
    }

    @Override
    public String login(Login command) {
        var user = userPort.findByEmail(command.email())
                .filter(candidate -> passwordHasher.matches(command.rawPassword(), candidate.passwordHash()))
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        return tokenIssuer.issue(user.userId());
    }
}
