package com.example.demo.unit.controller;

import com.example.demo.controller.GlobalExceptionHandler;
import com.example.demo.roles.RoleController;
import com.example.demo.roles.RoleDTO;
import com.example.demo.roles.RoleService;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.validation.Validator;

import javax.servlet.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {
	// turn off validation
	@Mock
	private Validator validator;
	
	@Mock
	private RoleService service;
	
	@InjectMocks
	private RoleController roleController;
	
	RoleDTO role1;
	RoleDTO role2;
	
	private Filter mockSpringSecurityFilter = new Filter(){
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			Filter.super.init(filterConfig);
		}
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					new MyUserDetails(1L, "User", "", List.of(new SimpleGrantedAuthority(RoleContainer.ADMIN)), true, false),
					"", Collections.emptyList());
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			chain.doFilter(request, response);
		}
		
		@Override
		public void destroy() {
			SecurityContextHolder.clearContext();
		}
		
		public void getFilters(MockHttpServletRequest mockHttpServletRequest){}
	};
	
	@BeforeEach
	void setUp() {
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(roleController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.apply(springSecurity(mockSpringSecurityFilter))
				.setValidator(validator);
		RestAssuredMockMvc.standaloneSetup(mvc);
		initData();
	}
	
	@AfterEach
	void tearDown() {
	}
	
	void initData() {
		role1 = RoleDTO.builder().id(1L).name("User").description("description").build();
		role2 = RoleDTO.builder().id(2L).name("Admin").description("description").build();
	}
	
	@Test
	void getAllShouldReturnAllEntities() {
		given(service.getAll())
				.willReturn(List.of(role1, role2));
		
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<RoleDTO> list = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.ROLES_PATH)
				.then()
					.status(HttpStatus.OK)
					.extract().as(List.class);
		//@formatter:on
		
		assertThat(list.size()).isEqualTo(2);
	}
	
	@Test
	void getByIdShouldReturnEntityIfItExists() {
		given(service.getById(role1.getId()))
				.willReturn(Optional.of(role1));
		
		//@formatter:off
		RoleDTO roleDTO = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.ROLES_PATH + "/" + role1.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(RoleDTO.class);
		//@formatter:on
		
		assertThat(roleDTO.getName()).isEqualTo(role1.getName());
	}
	
	@Test
	void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
		long id = 1;
		given(service.getById(id))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.ROLES_PATH + "/" + id)
				.then()
					.status(HttpStatus.NOT_FOUND);
		//@formatter:on
	}
	
	@Test
	void createShouldSucceedIfDataIsValid() {
		given(service.create(role1))
				.willReturn(role1);
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(role1)
					.contentType(ContentType.JSON)
				.when()
					.post(SecurityUtility.ROLES_PATH)
				.then()
					.status(HttpStatus.CREATED);
		//@formatter:on
	}
	
	@Test
	void updateShouldSucceedIfDataIsValid() {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("Unique")
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("User")
				.description("description")
				.build();
		given(service.update(eq(savedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		//@formatter:off
		RoleDTO roleDTO = RestAssuredMockMvc
				.given()
					.body(updatedRole)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.ROLES_PATH + "/" + savedRole.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(RoleDTO.class);
		//@formatter:on
		
		assertThat(roleDTO.getName()).isEqualTo(updatedRole.getName());
	}
	
	@Test
	void updateShouldFailIfEntityWasNotFound() {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("User")
				.description("description")
				.build();
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(updatedRole)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.ROLES_PATH + "/" + updatedRole.getId())
				.then()
					.status(HttpStatus.NOT_FOUND);
		//@formatter:on
	}
	
	@Test
	void deleteShouldSucceedIfEntityExists() {
		long id = 1;
		doNothing().when(service).deleteById(id);
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
				.when()
				.delete(SecurityUtility.ROLES_PATH + "/" + id)
				.then()
				.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}