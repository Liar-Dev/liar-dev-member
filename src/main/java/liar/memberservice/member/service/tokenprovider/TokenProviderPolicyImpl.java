package liar.memberservice.member.service.tokenprovider;

import liar.memberservice.member.domain.member.Authority;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import liar.memberservice.exception.exception.BadJwtRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TokenProviderPolicyImpl implements InitializingBean, TokenProviderPolicy {

    public static final String TOKEN_TYPE = "Bearer ";
    private static final String AUTHORITIES_KEY = "auth";

    private final String secretKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final long reissueRefreshTime;

    private Key key;

    public TokenProviderPolicyImpl(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expiration-time}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTime,
            @Value("${jwt.reissue-refresh-time}") long reissueRefreshTime) {
        this.secretKey = secretKey;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.reissueRefreshTime = reissueRefreshTime;
    }



    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenExpirationTime);
    }

    @Override
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenExpirationTime);
    }

    @Override
    public String createAccessToken(String userId, List<Authority> roles) {
        return createToken(userId, roles, accessTokenExpirationTime);
    }

    @Override
    public String createRefreshToken(String userId, List<Authority> roles) {
        return createToken(userId, roles, refreshTokenExpirationTime);
    }

    @Override
    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    @Override
    public long getRemainingTimeFromToken(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - (new Date()).getTime();
    }

    @Override
    public boolean isMoreThanReissueTime(String token) {
        return getRemainingTimeFromToken(token) >= reissueRefreshTime;
    }


    @Override
    public Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    @Override
    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new BadJwtRequestException();
        }
    }

    @Override
    public String removeType(String token) {

        if (token == null || token.length() < TOKEN_TYPE.length()) return null;
        return token.substring(TOKEN_TYPE.length());
    }

    @Override
    public Long getRemainTime(String token) {

        Claims claims = getClaims(token);
        if (claims == null || claims.getExpiration() == null) return null;
        return claims.getExpiration().getTime();
    }

    @Override
    public String createToken(Authentication authentication, long tokenTime) {

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(new Date())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(System.currentTimeMillis() + tokenTime))
                .compact();
    }

    @Override
    public String createToken(String userId, List<Authority> authorities, long tokenTime) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put(AUTHORITIES_KEY, getRoles(authorities));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenTime + new Random().nextInt(100000)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private List<String> getRoles (List<Authority> authorities) {

        List<String> roles = new ArrayList<>();
        authorities.forEach(role -> roles.add(role.getAuthorities().getAuthoritiesName()));
        return roles;
    }
}
