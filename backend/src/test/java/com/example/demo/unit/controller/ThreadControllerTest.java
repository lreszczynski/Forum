package com.example.demo.unit.controller;

import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.controller.GlobalExceptionHandler;
import com.example.demo.posts.dto.PostDTO;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.ThreadController;
import com.example.demo.threads.ThreadService;
import com.example.demo.threads.dto.CreateThreadDTO;
import com.example.demo.threads.dto.ThreadDTO;
import com.example.demo.users.dto.UserDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(MockitoExtension.class)
class ThreadControllerTest {
	// turn off validation
	@Mock
	private Validator validator;
	
	@Mock
	private ThreadService service;
	
	@InjectMocks
	private ThreadController threadController;
	
	PostDTO post;
	CategoryDTO category;
	UserDTO user;
	ThreadDTO threadDTO;
	CreateThreadDTO createThreadDTO;
	
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
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(threadController)
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
		user = UserDTO.builder().id(1L).username("user123").email("user@gmail.com").role(roleUserDTO).banned(false).active(true).build();
		category = CategoryDTO.builder().id(1L).name("Category").description("Description").active(true).build();
		threadDTO = ThreadDTO.builder().id(1L).user(user).active(true).category(category).build();
		post = PostDTO.builder().id(1L).content("Post content").thread(threadDTO).build();
		createThreadDTO = CreateThreadDTO.builder().categoryId(category.getId()).content("Post content").title("Title").build();
	}
	
	@Test
	void getAllShouldReturnAllEntities() {
		Page<ThreadDTO> threads = new PageImpl<>(List.of(threadDTO),PageRequest.of(0, 20),20);
		given(service.getAll(any()))
				.willReturn(threads);
		
		//@formatter:off
		List<ThreadDTO> response = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.THREADS_PATH)
				.then()
					.status(HttpStatus.OK)
					.extract().body().jsonPath().getList("content",ThreadDTO.class);
		//@formatter:on
		
		assertThat(response).contains(threadDTO);
	}
	
	@Test
	void getByIdShouldReturnEntityIfItExists() {
		given(service.findById(threadDTO.getId()))
				.willReturn(Optional.of(threadDTO));
		
		//@formatter:off
		ThreadDTO response = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.THREADS_PATH + "/" + threadDTO.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(ThreadDTO.class);
		//@formatter:on
		
		assertThat(response.getTitle()).isEqualTo(threadDTO.getTitle());
	}
	
	@Test
	void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
		long id = 1;
		given(service.findById(id))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.THREADS_PATH + "/" + id)
				.then()
					.status(HttpStatus.NOT_FOUND);
		//@formatter:on
	}
	
	@Test
	void createShouldSucceedIfDataIsValid() {
		given(service.create(eq(createThreadDTO), any()))
				.willReturn(Optional.ofNullable(threadDTO));
		
		//@formatter:off
		ThreadDTO response = RestAssuredMockMvc
				.given()
					.body(createThreadDTO)
					.contentType(ContentType.JSON)
				.when()
					.post(SecurityUtility.THREADS_PATH)
				.then()
					.status(HttpStatus.CREATED)
					.extract().as(ThreadDTO.class);
		//@formatter:on
		
		assertThat(response.getTitle()).isEqualTo(threadDTO.getTitle());
	}
	
	@Test
	void updateShouldSucceedIfDataIsValid() {
		// given
		ThreadDTO updatedThread = ThreadDTO.builder()
				.id(1L)
				.title("Updated content")
				.build();
		ThreadDTO savedThread = ThreadDTO.builder()
				.id(1L)
				.title("Saved content")
				.build();
		given(service.update(eq(savedThread.getId()), any(ThreadDTO.class)))
				.willReturn(Optional.of(updatedThread));
		
		//@formatter:off
		ThreadDTO result = RestAssuredMockMvc
				.given()
					.body(updatedThread)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.THREADS_PATH + "/" + savedThread.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(ThreadDTO.class);
		//@formatter:on
		
		assertThat(result.getTitle()).isEqualTo(updatedThread.getTitle());
	}
	
	@Test
	void updateShouldFailIfEntityWasNotFound() {
		// given
		ThreadDTO updatedThread = ThreadDTO.builder()
				.id(1L)
				.title("Updated content")
				.build();
		given(service.update(eq(updatedThread.getId()), any(ThreadDTO.class)))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(updatedThread)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.THREADS_PATH + "/" + updatedThread.getId())
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
					.delete(SecurityUtility.THREADS_PATH + "/" + id)
				.then()
					.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}