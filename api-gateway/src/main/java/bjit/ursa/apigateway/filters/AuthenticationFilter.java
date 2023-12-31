package bjit.ursa.apigateway.filters;

import bjit.ursa.apigateway.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final JwtService jwtService;


    public AuthenticationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract token from Authorization header
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            String token = null;
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                String errorMessage = "{\"error_message\": \"No token found\"}";
                exchange.getResponse().getWriter().write(errorMessage);
                return exchange.getResponse().setComplete();
            }
            token = authorizationHeader.substring(7);
            if (!jwtService.isTokenValid(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                String errorMessage = "{\"error_message\": \"Invalid or Expired Token or \"}";
                return exchange.getResponse().setComplete();
            }
            List<String> roles = jwtService.extractUserRoles(token);
            if (!roles.isEmpty() && roles.contains(config.getRole())) {
                return chain.filter(exchange);
            }
            if (config.getRole().equals("ANY_USER")) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            String errorMessage = "{\"error_message\": \"Not Authorized \"}";
            return exchange.getResponse().setComplete();
        };
    }

    public static class Config {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
