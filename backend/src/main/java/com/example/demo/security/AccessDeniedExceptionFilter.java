package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccessDeniedExceptionFilter extends OncePerRequestFilter {
	
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                             FilterChain fc) throws ServletException, IOException {
		try {
			fc.doFilter(request, response);
		} catch (AccessDeniedException e) {
			logger.error("ADE");
			fc.doFilter(request, response);
			/*RequestDispatcher requestDispatcher =
					getServletContext().getRequestDispatcher(redirecturl);
			requestDispatcher.forward(request, response);*/
			
		}
	}
}