package com.example.demo.integration.controller.categories;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import io.restassured.RestAssured;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@Sql({
		"classpath:database-scripts/delete.sql",
		"classpath:database-scripts/create.sql",
		"classpath:database-scripts/insert.sql"
})
class CategoryControllerTest {
	
	@LocalServerPort
	int port;
	
	private CategoryDTO category1, category2;
	private String token;
	
	void initData() {
		category1 = CategoryDTO.builder().id(1L).name("Announcements").description("description").active(true).build();
		category2 = CategoryDTO.builder().id(2L).name("General").description("description").active(true).build();
	}
	
	ImmutablePair<String, String> getCredentialsFor(String role) {
		switch (role) {
			case RoleContainer.USER:
				return new ImmutablePair<>("user123", "user123");
			case RoleContainer.MODERATOR:
				return new ImmutablePair<>("mod123", "mod123");
			case RoleContainer.ADMIN:
				return new ImmutablePair<>("admin123", "admin123");
		}
		return new ImmutablePair<>("", "");
	}
	
	String getJwtToken(String role) {
		ImmutablePair<String, String> credentials = getCredentialsFor(role);
		//@formatter:off
		return RestAssured
				.given()
					.contentType("multipart/form-data")
						.multiPart("username", credentials.getLeft())
						.multiPart("password", credentials.getRight())
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
		initData();
	}
	
	@AfterEach
	void tearDown() {
		System.out.println("tearDown");
	}
	
	@Nested
	@DisplayName("If user has a role USER")
	class UserTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void testGetAll() {
			//@formatter:off
			@SuppressWarnings("unchecked")
			List<CategoryDTO> list = RestAssured
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
			//@formatter:off
			@SuppressWarnings("unchecked")
			List<CategoryDTO> list = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/")
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(List.class);
			//@formatter:on
			
			assertThat(list.size()).isEqualTo(2);
		}
	}
}