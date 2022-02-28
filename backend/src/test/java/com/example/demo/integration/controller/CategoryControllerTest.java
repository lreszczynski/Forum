package com.example.demo.integration.controller;

import com.example.demo.categories.dto.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.roles.RoleService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.dto.ThreadProjDTO;
import com.example.demo.threads.ThreadService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	
	@Autowired
	private ThreadService threadService;
	
	@Autowired
	private RoleService roleService;
	
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
					.extract().path("accessToken");
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
			List<CategoryDTO> categoryDTOList = categoryService.findAll();
			
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
		void getAllPinnedThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 3;
			List<ThreadProjDTO> threadProjDTOList = threadService.getAllPinnedByCategoryId(id);
			
			//@formatter:off
			List<ThreadProjDTO> result = List.of(RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH + "/" + id + "/pinned-threads")
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(ThreadProjDTO[].class));
			//@formatter:on
			
			assertThat(result).isEqualTo(threadProjDTOList);
			assertThat(result).allMatch(threadProjDTO -> threadProjDTO.getThread().isPinned());
		}
		
		@Test
		void getAllThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 2;
			Page<ThreadProjDTO> threadProjDTOList = threadService.getAllByCategoryId(id, null);
			
			//@formatter:off
			List<ThreadProjDTO> threadProjDTOS = RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/threads")
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().body().jsonPath().getList("content",ThreadProjDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(threadProjDTOList.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.findAll().get(0);
			
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
			CategoryDTO category = uniqueCategory.withId(categoryService.findAll().get(0).getId());
			//@formatter:off
			RestAssured
					.given()
						.body(category)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnUnauthorized() {
			CategoryDTO saved = categoryService.findAll().get(0);
			//@formatter:off
			RestAssured
					.given()
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+saved.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Nested
		@DisplayName("Nested tests of categoryRoles eg. categories/{id}/roles")
		class CategoriesRolesTests {
			@Test
			void getAllRolesOfCategoryShouldReturnUnauthorized() {
				Long id = 1L;
				
				//@formatter:off
				RestAssured
						.given()
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.UNAUTHORIZED.value());
				//@formatter:on
			}
			
			@Test
			void addRoleToCategoryShouldReturnUnauthorized() {
				Long categoryId = 1L;
				Long roleId = 1L;
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				assertThat(roleToSave).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.UNAUTHORIZED.value());
				//@formatter:on
			}
			
			@Test
			void deleteRoleFromExistingCategoryShouldReturnUnauthorized() {
				Long categoryId = 2L;
				Long roleId = 2L;
				Optional<RoleDTO> roleToDelete = roleService.getById(roleId);
				assertThat(roleToDelete).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.body(roleToDelete.get()).contentType(ContentType.JSON)
						.when()
							.delete(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles/"+roleId)
						.then()
							.statusCode(HttpStatus.UNAUTHORIZED.value());
				//@formatter:on
			}
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
			List<CategoryDTO> categoryDTOList = categoryService.findAll();
			
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
		void getAllPinnedThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 3;
			List<ThreadProjDTO> threadProjDTOList = threadService.getAllPinnedByCategoryId(id);
			
			//@formatter:off
			List<ThreadProjDTO> result = List.of(RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH + "/" + id + "/pinned-threads")
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(ThreadProjDTO[].class));
			//@formatter:on
			
			assertThat(result).isEqualTo(threadProjDTOList);
			assertThat(result).allMatch(threadProjDTO -> threadProjDTO.getThread().isPinned());
		}
		
		@Test
		void getAllThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 2;
			Page<ThreadProjDTO> threadProjDTOList = threadService.getAllByCategoryId(id, null);
			
			//@formatter:off
			List<ThreadProjDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/threads")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadProjDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(threadProjDTOList.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.findAll().get(0);
			
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
			CategoryDTO saved = categoryService.findAll().get(0);
			CategoryDTO updated = saved.withDescription("new description");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+saved.getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnForbidden() {
			CategoryDTO saved = categoryService.findAll().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+saved.getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Nested
		@DisplayName("Nested tests of categoryRoles eg. categories/{id}/roles")
		class CategoriesRolesTests {
			@Test
			void getAllRolesOfCategoryShouldReturnForbidden() {
				Long id = 1L;
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.FORBIDDEN.value());
				//@formatter:on
			}
			
			@Test
			void addRoleToCategoryShouldReturnForbidden() {
				Long categoryId = 1L;
				Long roleId = 1L;
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				assertThat(roleToSave).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.FORBIDDEN.value());
				//@formatter:on
			}
			
			@Test
			void deleteRoleFromExistingCategoryShouldReturnForbidden() {
				Long categoryId = 2L;
				Long roleId = 2L;
				Optional<RoleDTO> roleToDelete = roleService.getById(roleId);
				assertThat(roleToDelete).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
						.auth().oauth2(token)
						.body(roleToDelete.get()).contentType(ContentType.JSON)
						.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles/"+roleId)
						.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
				//@formatter:on
			}
		}
	}
	
	@Nested
	@DisplayName("If user has a role MODERATOR")
	class ModeratorTests {
		Long categoryIdWhereModHasRights = 2L;
		Long categoryIdWhereModHDoesNotHaveRights = 1L;
		
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.MODERATOR);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			List<CategoryDTO> categoryDTOList = categoryService.findAll();
			
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
		void getAllPinnedThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 3;
			List<ThreadProjDTO> threadProjDTOList = threadService.getAllPinnedByCategoryId(id);
			
			//@formatter:off
			List<ThreadProjDTO> result = List.of(RestAssured
					.given()
					.when()
						.get(SecurityUtility.CATEGORIES_PATH + "/" + id + "/pinned-threads")
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(ThreadProjDTO[].class));
			//@formatter:on
			
			assertThat(result).isEqualTo(threadProjDTOList);
			assertThat(result).allMatch(threadProjDTO -> threadProjDTO.getThread().isPinned());
		}
		
		@Test
		void getAllThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 2;
			Page<ThreadProjDTO> threadProjDTOList = threadService.getAllByCategoryId(id, null);
			
			//@formatter:off
			List<ThreadProjDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/threads")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadProjDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(threadProjDTOList.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.findAll().get(0);
			
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
		void createShouldReturnBadRequestIfIdIsNotNull() {
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
		void createShouldReturnBadRequestIfNameIsNotUnique() {
			CategoryDTO category = uniqueCategory.withName(categoryService.findAll().get(0).getName());
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
		void createShouldReturnBadRequestIfNameIsNull() {
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
		void createShouldReturnBadRequestIfNameIsEmpty() {
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
		void createShouldReturnBadRequestIfNameIsTooLong() {
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
		void createShouldReturnBadRequestIfDescriptionIsNull() {
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
		void createShouldReturnBadRequestIfDescriptionIsEmpty() {
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
		void createShouldReturnBadRequestIfDescriptionIsTooShort() {
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
		void createShouldReturnBadRequestIfDescriptionIsTooLong() {
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
		void updateShouldSucceedIfDataIsValidAndModeratorHasRights() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName("New name that's unique");
			//@formatter:off
			CategoryDTO returnedCategory = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(returnedCategory).isEqualTo(updatedCategory);
		}
		
		@Test
		void updateShouldReturnForbiddenIfModeratorDoesNotHaveRights() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHDoesNotHaveRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName("New name that's unique");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnForbiddenIfEntityWasNotFound() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfIdIsNull() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withId(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsNotUnique() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			Optional<CategoryDTO> adminCategory = categoryService.findById(categoryIdWhereModHDoesNotHaveRights);
			assertThat(savedCategory).isNotEmpty();
			assertThat(adminCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName(adminCategory.get().getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsNull() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsEmpty() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsTooLong() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsNull() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsEmpty() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsTooShort() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsTooLong() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			CategoryDTO updatedCategory = savedCategory.get().withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExistsAndModHasRights() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHasRights);
			assertThat(savedCategory).isNotEmpty();
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnForbiddenIfEntityExistsAndModDoesNotHaveRights() {
			Optional<CategoryDTO> savedCategory = categoryService.findById(categoryIdWhereModHDoesNotHaveRights);
			assertThat(savedCategory).isNotEmpty();
			assertThat(savedCategory).isNotEmpty();
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.get().getId())
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnForbiddenIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Nested
		@DisplayName("Nested tests of categoryRoles eg. categories/{id}/roles")
		class CategoriesRolesTests {
			@Test
			void getAllRolesOfAnExistingCategoryShouldSucceed() {
				Long id = 1L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(id).map(categoryDTO -> {
					Optional<Set<RoleDTO>> rolesForCategory = categoryService.findRolesForCategory(categoryDTO);
					return rolesForCategory.orElse(Collections.emptySet());
				});
				assertThat(savedRoles).isNotEmpty();
				
				//@formatter:off
				Set<RoleDTO> roleDTOS = Set.of(RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.OK.value())
							.extract().as(RoleDTO[].class));
				//@formatter:on
				
				assertThat(roleDTOS).isEqualTo(savedRoles.get());
			}
			
			@Test
			void getAllRolesOfNonExistingCategoryShouldReturnNotFound() {
				Long id = Long.MAX_VALUE;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(id).map(categoryDTO -> {
					Optional<Set<RoleDTO>> rolesForCategory = categoryService.findRolesForCategory(categoryDTO);
					return rolesForCategory.orElse(Collections.emptySet());
				});
				assertThat(savedRoles).isEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.NOT_FOUND.value());
				//@formatter:on
			}
			
			@Test
			void addRoleToExistingCategoryShouldSucceed() {
				Long categoryId = categoryIdWhereModHasRights;
				Long roleId = 1L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findRolesForCategoryById(categoryId);
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				
				assertThat(savedRoles).isNotEmpty();
				assertThat(roleToSave).isNotEmpty();
				assertThat(savedRoles.get()).doesNotContain(roleToSave.get());
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.CREATED.value());
				//@formatter:on
				
				savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				assertThat(savedRoles).isNotEmpty();
				assertThat(savedRoles.get()).contains(roleToSave.get());
			}
			
			@Test
			void addRoleToNonExistingCategoryShouldReturnForbidden() {
				Long categoryId = Long.MAX_VALUE;
				Long roleId = 1L;
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				assertThat(roleToSave).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.FORBIDDEN.value());
				//@formatter:on
			}
			
			@Test
			void deleteRoleFromExistingCategoryShouldSucceed() {
				Long categoryId = 2L;
				Long roleId = 2L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				Optional<RoleDTO> roleToDelete = roleService.getById(roleId);
				
				assertThat(savedRoles).isNotEmpty();
				assertThat(roleToDelete).isNotEmpty();
				assertThat(savedRoles.get()).contains(roleToDelete.get());
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.delete(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles/"+roleId)
						.then()
							.statusCode(HttpStatus.NO_CONTENT.value());
				//@formatter:on
				savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				assertThat(savedRoles).isNotEmpty();
				assertThat(savedRoles.get()).doesNotContain(roleToDelete.get());
			}
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
			List<CategoryDTO> categoryDTOList = categoryService.findAll();
			
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
		void getAllPinnedThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 3;
			List<ThreadProjDTO> threadProjDTOList = threadService.getAllPinnedByCategoryId(id);
			
			//@formatter:off
			List<ThreadProjDTO> result = List.of(RestAssured
					.given()
					.when()
					.get(SecurityUtility.CATEGORIES_PATH + "/" + id + "/pinned-threads")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadProjDTO[].class));
			//@formatter:on
			
			assertThat(result).isEqualTo(threadProjDTOList);
			assertThat(result).allMatch(threadProjDTO -> threadProjDTO.getThread().isPinned());
		}
		
		@Test
		void getAllThreadsByCategoryIdShouldReturnAllEntities() {
			long id = 2;
			Page<ThreadProjDTO> threadProjDTOList = threadService.getAllByCategoryId(id, null);
			
			//@formatter:off
			List<ThreadProjDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/threads")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadProjDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(threadProjDTOList.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			CategoryDTO category = categoryService.findAll().get(0);
			
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
		void createShouldReturnBadRequestIfIdIsNotNull() {
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
		void createShouldReturnBadRequestIfNameIsNotUnique() {
			CategoryDTO category = uniqueCategory.withName(categoryService.findAll().get(0).getName());
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
		void createShouldReturnBadRequestIfNameIsNull() {
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
		void createShouldReturnBadRequestIfNameIsEmpty() {
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
		void createShouldReturnBadRequestIfNameIsTooLong() {
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
		void createShouldReturnBadRequestIfDescriptionIsNull() {
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
		void createShouldReturnBadRequestIfDescriptionIsEmpty() {
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
		void createShouldReturnBadRequestIfDescriptionIsTooShort() {
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
		void createShouldReturnBadRequestIfDescriptionIsTooLong() {
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
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("New name that's unique");
			//@formatter:off
			CategoryDTO returnedCategory = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(CategoryDTO.class);
			//@formatter:on
			assertThat(returnedCategory).isEqualTo(updatedCategory);
		}
		
		@Test
		void updateShouldReturnNotFoundIfEntityWasNotFound() {
			CategoryDTO category = uniqueCategory.withId(Long.MAX_VALUE);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(category).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+category.getId())
					.then()
						.statusCode(HttpStatus.NOT_FOUND.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfIdIsNull() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withId(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsNotUnique() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(categoryService.findAll().get(1).getName());
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsNull() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsEmpty() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfNameIsTooLong() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withName(StringUtils.repeat(' ', 51));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsNull() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(null);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsEmpty() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsTooShort() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 4));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnBadRequestIfDescriptionIsTooLong() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			CategoryDTO updatedCategory = savedCategory.withDescription(StringUtils.repeat(' ', 251));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedCategory).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			CategoryDTO savedCategory = categoryService.findAll().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedCategory.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnBadRequestIfEntityDoesNotExist() {
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+Long.MAX_VALUE)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Nested
		@DisplayName("Nested tests of categoryRoles eg. categories/{id}/roles")
		class CategoriesRolesTests {
			@Test
			void getAllRolesOfAnExistingCategoryShouldSucceed() {
				Long id = 1L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(id).map(categoryDTO -> {
					Optional<Set<RoleDTO>> rolesForCategory = categoryService.findRolesForCategory(categoryDTO);
					return rolesForCategory.orElse(Collections.emptySet());
				});
				assertThat(savedRoles).isNotEmpty();
				
				//@formatter:off
				Set<RoleDTO> roleDTOS = Set.of(RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.OK.value())
							.extract().as(RoleDTO[].class));
				//@formatter:on
				
				assertThat(roleDTOS).isEqualTo(savedRoles.get());
			}
			
			@Test
			void getAllRolesOfNonExistingCategoryShouldReturnNotFound() {
				Long id = Long.MAX_VALUE;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(id).map(categoryDTO -> {
					Optional<Set<RoleDTO>> rolesForCategory = categoryService.findRolesForCategory(categoryDTO);
					return rolesForCategory.orElse(Collections.emptySet());
				});
				assertThat(savedRoles).isEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.get(SecurityUtility.CATEGORIES_PATH+"/"+id+"/roles")
						.then()
							.statusCode(HttpStatus.NOT_FOUND.value());
				//@formatter:on
			}
			
			@Test
			void addRoleToExistingCategoryShouldSucceed() {
				Long categoryId = 1L;
				Long roleId = 1L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findRolesForCategoryById(categoryId);
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				
				assertThat(savedRoles).isNotEmpty();
				assertThat(roleToSave).isNotEmpty();
				assertThat(savedRoles.get()).doesNotContain(roleToSave.get());
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.CREATED.value());
				//@formatter:on
				
				savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				assertThat(savedRoles).isNotEmpty();
				assertThat(savedRoles.get()).contains(roleToSave.get());
			}
			
			@Test
			void addRoleToNonExistingCategoryShouldReturnNotFound() {
				Long categoryId = Long.MAX_VALUE;
				Long roleId = 1L;
				Optional<RoleDTO> roleToSave = roleService.getById(roleId);
				assertThat(roleToSave).isNotEmpty();
				
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
							.body(roleToSave.get()).contentType(ContentType.JSON)
						.when()
							.post(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles")
						.then()
							.statusCode(HttpStatus.NOT_FOUND.value());
				//@formatter:on
			}
			
			@Test
			void deleteRoleFromExistingCategoryShouldSucceed() {
				Long categoryId = 2L;
				Long roleId = 2L;
				Optional<Set<RoleDTO>> savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				Optional<RoleDTO> roleToDelete = roleService.getById(roleId);
				
				assertThat(savedRoles).isNotEmpty();
				assertThat(roleToDelete).isNotEmpty();
				assertThat(savedRoles.get()).contains(roleToDelete.get());
				//@formatter:off
				RestAssured
						.given()
							.auth().oauth2(token)
						.when()
							.delete(SecurityUtility.CATEGORIES_PATH+"/"+categoryId+"/roles/"+roleId)
						.then()
							.statusCode(HttpStatus.NO_CONTENT.value());
				//@formatter:on
				savedRoles = categoryService.findById(categoryId).map(categoryDTO ->
						categoryService.findRolesForCategory(categoryDTO).orElse(Collections.emptySet()));
				assertThat(savedRoles).isNotEmpty();
				assertThat(savedRoles.get()).doesNotContain(roleToDelete.get());
			}
		}
	}
}