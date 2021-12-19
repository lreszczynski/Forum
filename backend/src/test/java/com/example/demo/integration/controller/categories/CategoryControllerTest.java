package com.example.demo.integration.controller.categories;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class CategoryControllerTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private CategoryService categoryService;
	private CategoryDTO uniqueCategory;
	
	private String token;
	
	void initData() {
		uniqueCategory = CategoryDTO.builder().id(null).name("Unique Name").description("description").active(true).build();
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
	public void setUp() throws SQLException {
		System.out.println("setUp");
		Connection connection = dataSource.getConnection();
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/delete.sql"));
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/create.sql"));
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/insert.sql"));
		connection.close();
		RestAssured.port = port;
		initData();
	}
	
	@AfterEach
	void tearDown() {
		System.out.println("tearDown");
	}
	
	@Nested
	@DisplayName("If user is not logged in")
	class GuestTests {
		@Test
		void getAllShouldReturnAllEntities() {
			List<CategoryDTO> categoryDTOList = categoryService.getAll();
			
			//@formatter:off
			List<CategoryDTO> categoryDTOS = List.of(RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO[].class));
			//@formatter:on
			
			assertThat(categoryDTOS).isEqualTo(categoryDTOList);
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.getAll().get(0);
			
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			
			assertThat(category).isEqualTo(categoryDTO);
		}
		
		@Test
		void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.NOT_FOUND.value());
			//@formatter:on
		}
		
		@Test
		void createShouldReturnUnauthorized() {
			CategoryDTO category = uniqueCategory;
			//@formatter:off
			RestAssured
					.given()
						.body(category)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnUnauthorized() {
			CategoryDTO category = uniqueCategory.withId(categoryService.getAll().get(0).getId());
			//@formatter:off
			RestAssured
					.given()
						.body(category)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+category.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnUnauthorized() {
			CategoryDTO saved = categoryService.getAll().get(0);
			//@formatter:off
			RestAssured
					.given()
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+saved.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role USER")
	class UserTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			List<CategoryDTO> categoryDTOList = categoryService.getAll();
			
			//@formatter:off
			List<CategoryDTO> categoryDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO[].class));
			//@formatter:on
			
			assertThat(categoryDTOS).isEqualTo(categoryDTOList);
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.getAll().get(0);
			
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			
			assertThat(category).isEqualTo(categoryDTO);
		}
		
		@Test
		void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.NOT_FOUND.value());
			//@formatter:on
		}
		
		@Test
		void createShouldReturnForbidden() {
			CategoryDTO category = uniqueCategory;
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnForbidden() {
			CategoryDTO saved = categoryService.getAll().get(0);
			CategoryDTO updated = saved.withDescription("new description");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+saved.getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnForbidden() {
			CategoryDTO saved = categoryService.getAll().get(0);
			//@formatter:off
			RestAssured
					.given()
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+saved.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role ADMIN")
	class AdminTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.ADMIN);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			List<CategoryDTO> categoryDTOList = categoryService.getAll();
			
			//@formatter:off
			List<CategoryDTO> categoryDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO[].class));
			//@formatter:on
			
			assertThat(categoryDTOS).isEqualTo(categoryDTOList);
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.getAll().get(0);
			
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			
			assertThat(category).isEqualTo(categoryDTO);
		}
		
		@Test
		void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.NOT_FOUND.value());
			//@formatter:on
		}
		
		@Test
		void createShouldSucceedIfDataIsValid() {
			CategoryDTO category = uniqueCategory;
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(categoryDTO).isEqualTo(category.withId(categoryDTO.getId()));
		}
		
		@Test
		void createShouldFailIfIdIsNotNull() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsNotUnique() {
			CategoryDTO category = uniqueCategory.withName(categoryService.getAll().get(0).getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsNull() {
			CategoryDTO category = uniqueCategory.withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsEmpty() {
			CategoryDTO category = uniqueCategory.withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsTooLong() {
			CategoryDTO category = uniqueCategory.withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsNull() {
			CategoryDTO category = uniqueCategory.withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsEmpty() {
			CategoryDTO category = uniqueCategory.withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsTooShort() {
			CategoryDTO category = uniqueCategory.withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsTooLong() {
			CategoryDTO category = uniqueCategory.withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("New name that's unique");
			//@formatter:off
			CategoryDTO returnedCategory = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(returnedCategory).isEqualTo(updatedCategory);
		}
		
		@Test
		void updateShouldFailIfEntityWasNotFound() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+uniqueCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfIdIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withId(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfIdMismatch() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsNotUnique() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(categoryService.getAll().get(1).getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsEmpty() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsTooLong() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsEmpty() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsTooShort() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsTooLong() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldFailIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role MODERATOR")
	class ModeratorTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.MODERATOR);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			List<CategoryDTO> categoryDTOList = categoryService.getAll();
			
			//@formatter:off
			List<CategoryDTO> categoryDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO[].class));
			//@formatter:on
			
			assertThat(categoryDTOS).isEqualTo(categoryDTOList);
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.getAll().get(0);
			
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			
			assertThat(category).isEqualTo(categoryDTO);
		}
		
		@Test
		void getByIdShouldReturnNotFoundIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.NOT_FOUND.value());
			//@formatter:on
		}
		
		@Test
		void createShouldSucceedIfDataIsValid() {
			CategoryDTO category = uniqueCategory;
			//@formatter:off
			CategoryDTO categoryDTO = RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(categoryDTO).isEqualTo(category.withId(categoryDTO.getId()));
		}
		
		@Test
		void createShouldFailIfIdIsNotNull() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsNotUnique() {
			CategoryDTO category = uniqueCategory.withName(categoryService.getAll().get(0).getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsNull() {
			CategoryDTO category = uniqueCategory.withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsEmpty() {
			CategoryDTO category = uniqueCategory.withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfNameIsTooLong() {
			CategoryDTO category = uniqueCategory.withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsNull() {
			CategoryDTO category = uniqueCategory.withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsEmpty() {
			CategoryDTO category = uniqueCategory.withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsTooShort() {
			CategoryDTO category = uniqueCategory.withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfDescriptionIsTooLong() {
			CategoryDTO category = uniqueCategory.withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.CATEGORIES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("New name that's unique");
			//@formatter:off
			CategoryDTO returnedCategory = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(returnedCategory).isEqualTo(updatedCategory);
		}
		
		@Test
		void updateShouldFailIfEntityWasNotFound() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+uniqueCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfIdIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withId(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfIdMismatch() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsNotUnique() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(categoryService.getAll().get(1).getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsEmpty() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsTooLong() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsNull() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsEmpty() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsTooShort() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDescriptionIsTooLong() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			CategoryDTO savedCategory = categoryService.getAll().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldFailIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+ "/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
	}
}