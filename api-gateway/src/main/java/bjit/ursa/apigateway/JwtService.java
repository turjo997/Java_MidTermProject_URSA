package bjit.ursa.apigateway;




import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "703373367639792442264529482B4D6251655468576D5A7134743777217A2543";
    public String extractUserEmail(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractUserRoles(String token){
        final Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("authorities");
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyByte = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyByte);
    }


    public  boolean isTokenValid(String token){
        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return true;
        } catch (Exception e) {

            return false;
        }
    }


    private Date extractExpiration(String token){
        return  extractClaim(token, Claims::getExpiration);
    }
    private boolean isTokenExpired(String token) {
        return  extractExpiration(token).before(new Date());
    }
}

