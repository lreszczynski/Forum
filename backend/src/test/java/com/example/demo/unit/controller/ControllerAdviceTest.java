package com.example.demo.unit.controller;

import com.example.demo.categories.CategoryController;
import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.controller.GlobalExceptionHandler;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import javax.servlet.*;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(MockitoExtension.class)
class ControllerAdviceTest {
	@Mock
	private CategoryController categoryController;
	
	private CategoryDTO category1, category2;
	
	private Filter mockSpringSecurityFilter = new Filter() {
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			Filter.super.init(filterConfig);
		}
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(
							new MyUserDetails(1L, "User", "", List.of(new SimpleGrantedAuthority(RoleContainer.ADMIN)),
									true, false),
							"", Collections.emptyList());
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			chain.doFilter(request, response);
		}
		
		@Override
		public void destroy() {
			SecurityContextHolder.clearContext();
		}
		
		public void getFilters(MockHttpServletRequest mockHttpServletRequest) {
		}
	};
	
	@BeforeEach
	void setUp() {
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(categoryController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.apply(springSecurity(mockSpringSecurityFilter));
		RestAssuredMockMvc.standaloneSetup(mvc);
		initData();
	}
	
	@AfterEach
	void tearDown() {
	}
	
	void initData() {
	}
	
	@Test
	void testConstraintViolationException() {
		given(categoryController.getAll())
				.willThrow(new ConstraintViolationException("error", new HashSet<>()));
		
		//@formatter:off
		RestAssuredMockMvc
			.given()
			.when()
				.get(SecurityUtility.CATEGORIES_PATH)
			.then()
				.status(HttpStatus.BAD_REQUEST);
		//@formatter:on
	}
	
}