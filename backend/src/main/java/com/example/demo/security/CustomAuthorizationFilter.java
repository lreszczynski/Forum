package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.demo.security.SecurityUtility.LOGIN_PATH;
import static com.example.demo.security.SecurityUtility.TOKEN_REFRESH_PATH;
import static com.example.demo.utility.JWTUtility.parseToken;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
	private final UserDetailsService userDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (request.getServletPath().equals(LOGIN_PATH) || request.getServletPath().equals(TOKEN_REFRESH_PATH)) {
			filterChain.doFilter(request, response);
			return;
		}
		Jws<Claims> jws = parseToken(request, response);
		if (jws != null) {
			Claims body = jws.getBody();
			String username = body.getSubject();
			
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, String> roles = (LinkedHashMap<String, String>) body.get("roles", List.class).get(0);
			Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
			roles.forEach((str, role) -> authorities.add(new SimpleGrantedAuthority(role)));
			
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(userDetailsService.loadUserByUsername(username), null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			log.info("CustomAuthorizationFilter: passed for {}", username);
			
			filterChain.doFilter(request, response);
		} else {
			log.info("CustomAuthorizationFilter: did not pass");
			//SecurityContextHolder.getContext().setAuthentication(null);
			filterChain.doFilter(request, response);
		}
	}
}
