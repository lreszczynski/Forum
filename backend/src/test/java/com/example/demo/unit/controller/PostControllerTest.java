package com.example.demo.unit.controller;

import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.controller.GlobalExceptionHandler;
import com.example.demo.posts.PostController;
import com.example.demo.posts.PostService;
import com.example.demo.posts.dto.PostCreateDTO;
import com.example.demo.posts.dto.PostDTO;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.security.MyUserDetails;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
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
class PostControllerTest {
	// turn off validation
	@Mock
	private Validator validator;
	
	@Mock
	private PostService service;
	
	@InjectMocks
	private PostController postController;
	
	PostDTO post;
	CategoryDTO category;
	UserDTO user;
	ThreadDTO threadDTO;
	PostCreateDTO postCreateDTO;
	
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
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(postController)
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
		category = CategoryDTO.builder().name("Category").description("Description").active(true).build();
		threadDTO = ThreadDTO.builder().user(user).active(true).category(category).build();
		post = PostDTO.builder().id(1L).content("Post content").thread(threadDTO).build();
		postCreateDTO = PostCreateDTO.builder().threadId(threadDTO.getId()).content("Post Content").build();
	}
	
	@Test
	void getAllShouldReturnAllEntities() {
		Page<PostDTO> posts = new PageImpl<>(List.of(post),PageRequest.of(0, 20),20);
		given(service.getAll(any()))
				.willReturn(posts);
		
		//@formatter:off
		List<PostDTO> response = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.POSTS_PATH)
				.then()
					.status(HttpStatus.OK)
					.extract().body().jsonPath().getList("content",PostDTO.class);
		//@formatter:on
		
		assertThat(response).contains(post);
	}
	
	@Test
	void getByIdShouldReturnEntityIfItExists() {
		given(service.getById(post.getId()))
				.willReturn(Optional.of(post));
		
		//@formatter:off
		PostDTO postDTO = RestAssuredMockMvc
				.given()
				.when()
					.get(SecurityUtility.POSTS_PATH + "/" + post.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(PostDTO.class);
		//@formatter:on
		
		assertThat(postDTO.getContent()).isEqualTo(post.getContent());
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
					.get(SecurityUtility.POSTS_PATH + "/" + id)
				.then()
					.status(HttpStatus.NOT_FOUND);
		//@formatter:on
	}
	
	@Test
	void createShouldSucceedIfDataIsValid() {
		given(service.create(eq(postCreateDTO), any()))
				.willReturn(Optional.ofNullable(post));
		
		//@formatter:off
		PostDTO postDTO = RestAssuredMockMvc
				.given()
					.body(postCreateDTO)
					.contentType(ContentType.JSON)
				.when()
					.post(SecurityUtility.POSTS_PATH)
				.then()
					.status(HttpStatus.CREATED)
					.extract().as(PostDTO.class);
		//@formatter:on
		
		assertThat(postDTO.getContent()).isEqualTo(post.getContent());
	}
	
	@Test
	void updateShouldSucceedIfDataIsValid() {
		// given
		PostDTO updatedPost = PostDTO.builder()
				.id(1L)
				.content("Updated content")
				.build();
		PostDTO savedPost = PostDTO.builder()
				.id(1L)
				.content("Saved content")
				.build();
		given(service.update(eq(savedPost.getId()), any(PostDTO.class)))
				.willReturn(Optional.of(updatedPost));
		
		//@formatter:off
		PostDTO result = RestAssuredMockMvc
				.given()
					.body(updatedPost)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.POSTS_PATH + "/" + savedPost.getId())
				.then()
					.status(HttpStatus.OK)
					.extract().as(PostDTO.class);
		//@formatter:on
		
		assertThat(result.getContent()).isEqualTo(updatedPost.getContent());
	}
	
	@Test
	void updateShouldFailIfEntityWasNotFound() {
		// given
		PostDTO updatedPost = PostDTO.builder()
				.id(1L)
				.content("Updated content")
				.build();
		given(service.update(eq(updatedPost.getId()), any(PostDTO.class)))
				.willReturn(Optional.empty());
		
		//@formatter:off
		RestAssuredMockMvc
				.given()
					.body(updatedPost)
					.contentType(ContentType.JSON)
				.when()
					.put(SecurityUtility.POSTS_PATH + "/" + updatedPost.getId())
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
					.delete(SecurityUtility.POSTS_PATH + "/" + id)
				.then()
					.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}