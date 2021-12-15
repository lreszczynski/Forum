package com.example.demo.controller;

import com.example.demo.utility.JWTUtility;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.example.demo.utility.JWTUtility.getTokenFromRequest;
import static com.example.demo.utility.JWTUtility.parseToken;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("token")
@Slf4j
public class TokenController {
	@Operation(summary = "Generate a new access token")
	@ApiResponse(responseCode = "200", description = "Token was generated", content = {
			@Content(mediaType = APPLICATION_JSON_VALUE)
	})
	@GetMapping(value = "/refresh", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Object> generateAccessToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = getTokenFromRequest(request);
		
		Jws<Claims> jws = parseToken(request, response);
		if (jws != null) {
			Claims body = jws.getBody();
			String username = body.getSubject();
			@SuppressWarnings("unchecked")
			Collection<GrantedAuthority> roles = body.get("roles", Collection.class);
			String accessToken = JWTUtility.generateToken(request.getRequestURI(), username, roles, JWTUtility.TOKEN_TYPE.ACCESS);
			
			Map<String, String> tokens = new HashMap<>();
			tokens.put("access_token", accessToken);
			tokens.put("refresh_token", refreshToken);
			return ResponseEntity.ok(tokens);
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh token expired.");
	}
}