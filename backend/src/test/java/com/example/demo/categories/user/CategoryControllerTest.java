package com.example.demo.categories.user;

import com.example.demo.categories.CategoryController;
import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.integration.controller.TestConfigUserDetailsService;
import com.example.demo.security.SecurityUtility;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfigUserDetailsService.class)
@TestInstance(Lifecycle.PER_CLASS)
//@Sql({"classpath:database-scripts/delete.sql", "classpath:database-scripts/create.sql"})
class CategoryControllerTest {
	
	@LocalServerPort
	int port;
	
	@MockBean
	PasswordEncoder passwordEncoder;
	
	@MockBean
	private CategoryService service;
	
	@InjectMocks
	private CategoryController categoryController;
	
	private CategoryDTO category1, category2;
	private String token;
	
	void initData() {
		category1 = CategoryDTO.builder().id(1L).name("Announcements").description("description").active(true).build();
		category2 = CategoryDTO.builder().id(2L).name("General").description("description").active(true).build();
	}
	
	String getJwtToken() {
		//@formatter:off
		return RestAssured
				.given()
					.contentType("multipart/form-data")
						.multiPart("username", "Moderator")
						.multiPart("password", "mocked")
				.when()
					.post(SecurityUtility.LOGIN_PATH)
				.then()
					.statusCode(HttpStatus.OK.value())
					.extract().path("access_token");
		//@formatter:on
	}
	
	@BeforeEach
	public void setUp() {
		System.out.println("setUp");
		RestAssured.port = port;
		given(passwordEncoder.matches(any(), any())).willReturn(true);
		initData();
		token = getJwtToken();
	}
	
	@AfterEach
	void tearDown() {
		System.out.println("tearDown");
	}
	
	@Test
	void testGetAll() {
		given(service.getAll())
				.willReturn(List.of(category1, category2));
		
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<CategoryDTO> list = io.restassured.RestAssured
				.given()
					.auth().oauth2(token)
				.when()
					.get(SecurityUtility.CATEGORIES_PATH)
				.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(List.class);
		//@formatter:on
		
		assertThat(list.size()).isEqualTo(2);
	}
	
	@Test
	void testGetById() {
		given(service.getById(category1.getId()))
				.willReturn(Optional.of(category1));
		
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<CategoryDTO> list = io.restassured.RestAssured
				.given()
					.auth().oauth2(token)
				.when()
					.get(SecurityUtility.CATEGORIES_PATH + category2.getId())
				.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(List.class);
		//@formatter:on
		
		assertThat(list.size()).isEqualTo(2);
	}
}