package Car.project.Security;

import Car.project.Entities.User;
import Car.project.Repositories.ClientRepository; 
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; 
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "Oq3F9WzKQ4ZjJsN7qV4m6P2Tb/XkGJ0H2n8VYx6o4TQ=";
    private final long EXPIRATION_TIME = 86400000;  // 1 jour

    @Autowired 
    private ClientRepository clientRepository;

    @Transactional(readOnly = true) 
    public String generateToken(User user) {
        
        Long clientId = clientRepository.findClientIdByUserId(user.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", user.getRoles().stream()
                .map(role -> role.getNom().name())
                .collect(Collectors.toList()));
        
        claims.put("user_id", user.getId());
        claims.put("client_id", clientId);


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}