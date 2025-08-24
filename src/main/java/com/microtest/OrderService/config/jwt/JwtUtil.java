package com.microtest.OrderService.config.jwt;


//import java.security.Key;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.access.secret}")
    private String accessSecret;
    @Value("${jwt.access.expiration}")
    private Long accessExp;

    //<editor-fold defaultState="collapsed" desc="UserId + JTI + Role">
    public String subject_access(String token) {
        return parseAccess(token).getBody().getSubject();
    }

    public String role(String token) {
        return parseAccess(token).getBody().get("role", String.class);
    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Is Valid jwt">
    public boolean isAccessValid(String jwt) {
        return isValid(jwt);
    }

    private boolean isValid(String jwt) {
        try {
            parseAccess(jwt);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Claims">
    public Jws<Claims> parseAccess(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey_access())
                .build()
                .parseClaimsJws(jwt);
    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Key Secret">
    private SecretKey getSignKey_access() {
        byte[] keyBytes = Decoders.BASE64.decode(accessSecret);
        return new SecretKeySpec(keyBytes, (SignatureAlgorithm.HS256).getJcaName());
    }
    //</editor-fold>

}
