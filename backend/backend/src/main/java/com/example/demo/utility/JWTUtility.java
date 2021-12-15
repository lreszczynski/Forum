package com.example.demo.utility;

import com.example.demo.security.SecurityUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_SECOND;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class JWTUtility {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public enum TOKEN_TYPE {ACCESS, REFRESH}
	
	public static String generateToken(String uri, String username, Collection<GrantedAuthority> roles, TOKEN_TYPE type) {
		Date expirationDate;
		switch (type) {
			case REFRESH:
				expirationDate = new Date(System.currentTimeMillis() + 2 * 60 * MILLIS_PER_SECOND);
				break;
			case ACCESS:
			default:
				expirationDate = new Date(System.currentTimeMillis() + 20 * MILLIS_PER_SECOND);
		}
		return Jwts.builder()
				.setIssuer(uri)
				.setSubject(username)
				.claim("roles", roles)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(expirationDate)
				.signWith(SecurityUtility.SECRET_KEY)
				.compact();
	}
	
	public static String getTokenFromRequest(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring("Bearer ".length());
		}
		return null;
	}
	
	public static Jws<Claims> parseToken(HttpServletRequest request, HttpServletResponse response) {
		try {
			String token = getTokenFromRequest(request);
			if (token == null) {
				return null;
			}
			return Jwts.parserBuilder()
					.setSigningKey(SecurityUtility.SECRET_KEY)
					.build()
					.parseClaimsJws(token);
		} catch (JwtException ex) {
			log.error("Error logging in: {}", ex.getMessage());
			/*response.setHeader("error", ex.getMessage());
			response.setStatus(HttpStatus.FORBIDDEN.value());
			
			Map<String, String> error = new HashMap<>();
			error.put("error_message", ex.getMessage());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			try {
				objectMapper.writeValue(response.getOutputStream(), error);
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		return null;
	}
}
