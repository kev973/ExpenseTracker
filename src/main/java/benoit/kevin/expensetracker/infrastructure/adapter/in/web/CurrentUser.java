package benoit.kevin.expensetracker.infrastructure.adapter.in.web;

import benoit.kevin.expensetracker.domain.user.UserId;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * The caller's identity comes from the validated token subject, never from the request body.
 */
final class CurrentUser {

    private CurrentUser() {}

    static UserId of(Jwt jwt) {
        return new UserId(UUID.fromString(jwt.getSubject()));
    }
}
