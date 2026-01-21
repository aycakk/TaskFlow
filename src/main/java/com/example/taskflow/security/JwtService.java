package com.example.taskflow.security;

import com.example.taskflow.infrastructure.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationMs = expirationMs;
    }

    /** Token üretir: subject=username, claim:userId */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // ✅ userId claim (UserEntity implements UserDetails ise direkt alabiliyoruz)
        if (userDetails instanceof UserEntity u) {
            claims.put("userId", u.getId());
        }


        // claims.put("role", "USER");

        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(extraClaims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Token içinden tüm claims’leri güvenli şekilde parse eder (signature doğrular). */
    public Claims getClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Token’dan tek bir bilgi çekmek için generic helper */
    public <T> T extract(String token, Function<Claims, T> resolver) throws JwtException {
        return resolver.apply(getClaims(token));
    }

    public String extractUsername(String token) throws JwtException {
        return extract(token, Claims::getSubject);
    }

    public Long extractUserId(String token) throws JwtException {
        Object v = extract(token, c -> c.get("userId"));
        if (v == null) return null;

        // bazı durumlarda Integer gelebilir
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l) return l;
        if (v instanceof String s) return Long.parseLong(s);

        throw new IllegalArgumentException("Invalid userId claim type: " + v.getClass());
    }

    /**  Süresi doldu mu? (doğru kontrol) */
    public boolean isTokenExpired(String token) throws JwtException {
        Date expiration = extract(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /** Token bu user’a ait mi ve süresi geçerli mi? */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
