package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(new InMemoryPorts.Users(), new InMemoryPorts.PlainHasher(),
                new InMemoryPorts.Tokens(), new InMemoryPorts.Ids());
    }

    private AuthUseCase.Register registration() {
        return new AuthUseCase.Register("Kevin", "Benoit", "kev@example.com", "password1");
    }

    @Test
    void registrationReturnsAToken() {
        assertNotNull(authService.register(registration()));
    }

    @Test
    void aDuplicateEmailIsRejected() {
        authService.register(registration());

        assertThrows(IllegalArgumentException.class, () -> authService.register(registration()));
    }

    @Test
    void aShortPasswordIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AuthUseCase.Register("Kevin", "Benoit", "kev@example.com", "short"));
    }

    @Test
    void anUnknownEmailAndAWrongPasswordFailIdentically() {
        authService.register(registration());

        var unknownEmail = assertThrows(IllegalArgumentException.class,
                () -> authService.login(new AuthUseCase.Login("nobody@example.com", "password1")));
        var wrongPassword = assertThrows(IllegalArgumentException.class,
                () -> authService.login(new AuthUseCase.Login("kev@example.com", "wrongpassword")));

        assertEquals(unknownEmail.getMessage(), wrongPassword.getMessage());
    }

    @Test
    void theRightPasswordLogsIn() {
        var registered = authService.register(registration());
        var loggedIn = authService.login(new AuthUseCase.Login("kev@example.com", "password1"));

        assertEquals(registered, loggedIn);
    }
}
