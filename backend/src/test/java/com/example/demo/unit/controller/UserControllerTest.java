package com.example.demo.unit.controller;

import com.example.demo.controller.GlobalExceptionHandler;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.users.UserController;
import com.example.demo.users.UserService;
import com.example.demo.users.dto.UserDTO;
import com.example.demo.users.dto.UserProfileDTO;
import com.example.demo.users.dto.UserRegistrationDTO;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
	// turn off validation
	@Mock
	private Validator validator;
	
	@Mock
	private UserService service;
	
	@InjectMocks
	private UserController userController;
	
	private UserDTO user1, user2;
	private UserRegistrationDTO userRegistrationDTO;
	
	private Filter mockSpringSecurityFilter = new Filter(){
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			Filter.super.init(filterConfig);
		}
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					new MyUserDetails(1L, "Admin", "", List.of(new SimpleGrantedAuthority(RoleContainer.ADMIN)), true, false),
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
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(userController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.apply(springSecurity(mockSpringSecurityFilter))
				.setValidator(validator);
		RestAssuredMockMvc.standaloneSetup(mvc);
		initData();
	}
	
	@AfterEach
	void tearDown() {
	}
	
	void initData() {
		RoleDTO roleUserDTO = RoleDTO.builder().id(1L).name("User").description("description").build();
		RoleDTO roleAdminDTO = RoleDTO.builder().id(2L).name("Admin").description("description").build();
		user1 = UserDTO.builder().id(1L).username("user123").email("user@gmail.com").role(roleUserDTO).banned(false).active(true).build();
		user2 = UserDTO.builder().id(2L).username("user234").email("admin@gmail.com").role(roleAdminDTO).banned(false).active(true).build();
		userRegistrationDTO = UserRegistrationDTO.builder()
				.username("user123")
				.password("pass123")
				.email("user@gmail.com")
				.build();
	}
	
	@Test
	void getAllShouldReturnAllEntities() {
		Page<UserDTO> users = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20),20);
		given(service.getAll(any()))
				.willReturn(users);
		
		//@formatter:off
		List<UserDTO> response = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.USERS_PATH)
				.then()
					.status(HttpStatus.OK)
					.extract().body().jsonPath().getList("content",UserDTO.class);
		//@formatter:on
		
		assertThat(response).contains(user1, user2);
	}
	
	@Test
	void getUserProfileByIdShouldReturnEntityIfItExists() {
		given(service.getUserPublicProfile(user1.getId()))
				.willReturn(Optional.of(UserProfileDTO.builder().username(user1.getUsername()).build()));
		
		//@formatter:off
		UserDTO userDTO = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.USERS_PATH + "/profile/" + user1.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(UserDTO.class);
		//@formatter:on
		
		assertThat(userDTO.getUsername()).isEqualTo(user1.getUsername());
	}
	
	@Test
	void getUserProfileByIdShouldReturnNotFoundIfEntityDoesNotExist() {
		long id = 1;
		given(service.getUserPublicProfile(id))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.USERS_PATH + "/profile/" + id)
				.then()
					.status(HttpStatus.NOT_FOUND);
		//@formatter:on
	}
	
	@Test
	void getUserSettingsShouldReturnLoggedInUserDetails() {
		long id = 1;
		given(service.getUserAccount(id))
				.willReturn(Optional.of(user1));
		
		//@formatter:off
		UserDTO userDTO = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.USERS_PATH + "/account")
				.then()
					.status(HttpStatus.OK)
					.extract().as(UserDTO.class);
		//@formatter:on
		
		assertThat(userDTO.getUsername()).isEqualTo(user1.getUsername());
	}
	
	@Test
	void registerShouldSucceedIfDataIsValid() {
		given(service.register(userRegistrationDTO))
				.willReturn(user1);
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(userRegistrationDTO)
					.contentType(ContentType.JSON)
				.when()
					.post(SecurityUtility.USERS_PATH + "/register")
				.then()
					.status(HttpStatus.CREATED);
		//@formatter:on
	}
	
	@Test
	void updateShouldSucceedIfDataIsValid() {
		// given
		UserDTO updatedUser = UserDTO.builder()
				.id(1L)
				.username("user123")
				.email("user123@gmail.com")
				.build();
		UserDTO savedUser = UserDTO.builder()
				.id(1L)
				.username("user123")
				.email("user@gmail.com")
				.build();
		given(service.update(any(UserDTO.class)))
				.willReturn(Optional.of(updatedUser));
		
		//@formatter:off
		UserDTO userDTO = RestAssuredMockMvc
				.given()
					.body(updatedUser)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.USERS_PATH + "/" + savedUser.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(UserDTO.class);
		//@formatter:on
		
		assertThat(userDTO.getEmail()).isEqualTo(updatedUser.getEmail());
	}
	
	@Test
	void updateShouldReturnNotFoundIfEntityWasNotFound() {
		// given
		UserDTO updatedUser = UserDTO.builder()
				.id(1L)
				.username("user123")
				.email("user@gmail.com")
				.build();
		given(service.update(any(UserDTO.class)))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(updatedUser)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.USERS_PATH + "/" + updatedUser.getId())
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
					.delete(SecurityUtility.USERS_PATH + "/" + id)
				.then()
					.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}
