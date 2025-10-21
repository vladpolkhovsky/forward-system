package by.forward.forward_system.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import by.forward.forward_system.core.dto.auth.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtRelatedThings {

    private final static TypeReference<Map<String, Object>> mapperTypeReference = new TypeReference<>() {
    };

    private final JwtProperties jwtProperties;
    private final ObjectMapper mapper;

    private static Date toDate(LocalDateTime local) {
        Instant instant = local.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public HttpHeaders createSuccessfulSignInHeaders(UserDto userDto) {
        return HttpHeaders.readOnlyHttpHeaders(fromSingleValue(Map.of(HttpHeaders.SET_COOKIE, toCookie(createJwtToken(userDto)))));
    }

    public HttpHeaders createLogoutHeaders() {
        return HttpHeaders.readOnlyHttpHeaders(fromSingleValue(Map.of(HttpHeaders.SET_COOKIE, toResetCookie())));
    }

    private static MultiValueMap<String, String> fromSingleValue(Map<String, String> map) {
        LinkedMultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        map.forEach(mvm::add);
        return mvm;
    }

    private String toCookie(String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getJwtCookieName(), token)
            .httpOnly(true)
            .maxAge(Duration.ofDays(365))
            .sameSite("Lax")
            .path("/")
            .build();
        return cookie.toString();
    }

    private String toResetCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getJwtCookieName(), "")
            .httpOnly(true)
            .maxAge(0)
            .sameSite("Lax")
            .path("/")
            .build();
        return cookie.toString();
    }

    private String createJwtToken(UserDto userDto) {
        return Jwts.builder()
            .subject(userDto.getUsername())
            .claims(mapper.convertValue(userDto, mapperTypeReference))
            .expiration(toDate(LocalDateTime.now().plusMonths(6)))
            .issuedAt(toDate(LocalDateTime.now()))
            .signWith(getSignKey())
            .compact();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getJwtSigningKey().getBytes(StandardCharsets.UTF_8));
    }

    public UserDto parseJwtToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(getSignKey()).build()
            .parseSignedClaims(token);
        Claims payload = claimsJws.getPayload();
        return mapper.convertValue(payload, UserDto.class);
    }

    @Data
    @Component
    public static class JwtProperties {
        @Value("${jwt.signing.key}")
        private String jwtSigningKey;
        @Value("${jwt.cookie.name}")
        private String jwtCookieName;
    }
}
