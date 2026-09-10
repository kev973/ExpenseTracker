package benoit.kevin.expensetracker.infrastructure.adapter.out.security;

import benoit.kevin.expensetracker.application.port.out.TokenIssuer;
import benoit.kevin.expensetracker.domain.user.UserId;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private static final Duration VALIDITY = Duration.ofHours(24);

    private final JwtEncoder jwtEncoder;

    public JwtTokenIssuer(JwtEncoder jwtEncoder) {
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder);
    }

    @Override
    public String issue(UserId userId) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("expense-tracker")
                .issuedAt(now)
                .expiresAt(now.plus(VALIDITY))
                .subject(userId.id().toString())
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
