package com.example.demo.unit.controller;

import com.example.demo.categories.CategoryController;
import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.controller.GlobalExceptionHandler;
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
class CategoryControllerTest {
	// turn off validation
	@Mock
	private Validator validator;
	
	@Mock
	private CategoryService service;
	
	@InjectMocks
	private CategoryController categoryController;
	
	private CategoryDTO category1, category2;
	
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
		StandaloneMockMvcBuilder mvc = MockMvcBuilders.standaloneSetup(categoryController)
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
		category1 = CategoryDTO.builder().id(1L).name("Announcements").description("description").active(true).build();
		category2 = CategoryDTO.builder().id(2L).name("General").description("description").active(true).build();
	}
	
	@Test
	void getAllShouldReturnAllEntities() {
		given(service.findAll())
				.willReturn(List.of(category1, category2));
		
		//@formatter:off
			@SuppressWarnings("unchecked")
			List<CategoryDTO> list = RestAssuredMockMvc
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH)
					.then()
						.status(HttpStatus.OK)
						.extract().as(List.class);
			//@formatter:on
		
		assertThat(list.size()).isEqualTo(2);
	}
	
	@Test
	void getByIdShouldReturnEntityIfItExists() {
		given(service.findById(category1.getId()))
				.willReturn(Optional.of(category1));
		
		//@formatter:off
			CategoryDTO categoryDTO = RestAssuredMockMvc
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH + "/" + category1.getId())
					.then()
						.status(HttpStatus.OK)
						.extract().as(CategoryDTO.class);
			//@formatter:on
		
		assertThat(categoryDTO.getName()).isEqualTo(category1.getName());
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
						.get(SecurityUtility.CATEGORIES_PATH + "/" + id)
					.then()
						.status(HttpStatus.NOT_FOUND);
			//@formatter:on
	}
	
	@Test
	void createShouldSucceedIfEntityWasSaved() {
		given(service.create(eq(category1)))
				.willReturn(category1);
		
		//@formatter:off
			RestAssuredMockMvc
					.given()
						.body(category1)
						.contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.status(HttpStatus.CREATED);
			//@formatter:on
	}
	
	@Test
	void updateShouldSucceedIfDataIsValid() {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Unique")
				.description("description")
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("User")
				.description("description")
				.build();
		given(service.update(eq(savedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		//@formatter:off
			CategoryDTO categoryDTO = RestAssuredMockMvc
					.given()
						.body(updatedCategory)
						.contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH + "/" + savedCategory.getId())
					.then()
						.status(HttpStatus.OK)
						.extract().as(CategoryDTO.class);
			//@formatter:on
		
		assertThat(categoryDTO.getName()).isEqualTo(updatedCategory.getName());
	}
	
	@Test
	void updateShouldFailIfEntityWasNotFound() {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("User")
				.description("description")
				.build();
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.empty());
		
		//@formatter:off
			RestAssuredMockMvc
					.given()
						.body(updatedCategory)
						.contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH + "/" + updatedCategory.getId())
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
					.delete(SecurityUtility.CATEGORIES_PATH + "/" + id)
				.then()
					.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}