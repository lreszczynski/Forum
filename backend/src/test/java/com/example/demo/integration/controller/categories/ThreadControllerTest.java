package com.example.demo.integration.controller.categories;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import com.example.demo.roles.RoleService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.ThreadCreateDTO;
import com.example.demo.threads.ThreadDTO;
import com.example.demo.threads.ThreadService;
import com.example.demo.users.UserService;
import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class ThreadControllerTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ThreadService threadService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	
	private ThreadCreateDTO validThread;
	
	private CategoryDTO existingCategory;
	
	Long inactiveCategoryId = 4L;
	
	private String token;
	
	void initData() {
		Optional<CategoryDTO> optionalCategoryDTO = categoryService.findById(3L);
		optionalCategoryDTO.ifPresent(categoryDTO -> existingCategory = categoryDTO);
		validThread = ThreadCreateDTO.builder().title("Title").content("Long enough content").categoryId(existingCategory.getId()).build();
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
		Long anyCategoryId = 3L;
		Long anyThreadId = 6L;
		
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<ThreadDTO> page = threadService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<ThreadDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnAllEntities() {
			Optional<ThreadDTO> byId = threadService.findById(1L);
			assertThat(byId).isNotEmpty();
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.when()
					.get(SecurityUtility.THREADS_PATH+"/"+byId.get().getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(byId.get());
		}
		
		@Test
		void createShouldFail() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(anyCategoryId);
			
			//@formatter:off
			RestAssured
					.given()
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfUserDidNotCreateThread() {
			ThreadDTO savedThread = threadService.findById(anyThreadId).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			RestAssured
					.given()
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldFail() {
			ThreadDTO savedThread = threadService.findById(anyThreadId).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.when()
					.delete(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role USER")
	class UserTests {
		Long categoryIdWhereUserHasRights = 3L;
		Long categoryIdWhereUserDoesNotHaveRights = 1L;
		Long threadIdCreatedByUser = 6L;
		Long threadIdCreatedBySomeoneElse = 3L;
		
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<ThreadDTO> page = threadService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<ThreadDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnAllEntities() {
			Optional<ThreadDTO> byId = threadService.findById(1L);
			assertThat(byId).isNotEmpty();
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.THREADS_PATH+"/"+byId.get().getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(byId.get());
		}
		
		@Test
		void createShouldSucceedIfDataIsValidAndUserHasRights() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(categoryIdWhereUserHasRights);
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.CREATED.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(threadCreateDTO.getTitle());
		}
		
		@Test
		void createShouldFailIfCategoryIsInactive() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(inactiveCategoryId);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfTitleIsEmpty() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle("");
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfTitleIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle(Strings.repeat(" ",81));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",9));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",10001));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfUserDoesNotHaveRights() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(categoryIdWhereUserDoesNotHaveRights);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailEvenIfUserCreatedThread() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedByUser).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfUserDidNotCreateThread() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedBySomeoneElse).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldFail() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedByUser).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role MODERATOR")
	class ModeratorTests {
		Long categoryIdWhereModHasRights = 2L;
		Long categoryIdWhereModDoesNotHaveRights = 1L;
		Long threadIdCreatedByModerator = 2L;
		Long threadIdCreatedBySomeoneElse = 6L;
		
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.MODERATOR);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<ThreadDTO> page = threadService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<ThreadDTO> threadProjDTOS = RestAssured
					.given()
					.when()
					.get(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",ThreadDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnAllEntities() {
			Optional<ThreadDTO> byId = threadService.findById(1L);
			assertThat(byId).isNotEmpty();
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.THREADS_PATH+"/"+byId.get().getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(byId.get());
		}
		
		@Test
		void createShouldSucceedIfDataIsValidAndModeratorHasRights() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(categoryIdWhereModHasRights);
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.CREATED.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(threadCreateDTO.getTitle());
		}
		
		@Test
		void createShouldFailIfCategoryIsInactive() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(inactiveCategoryId);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfTitleIsEmpty() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle("");
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfTitleIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle(Strings.repeat(" ",81));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",9));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",10001));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfModeratorDoesNotHaveRights() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(categoryIdWhereModDoesNotHaveRights);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.THREADS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValidAndModeratorHasRightsAndModeratorCreatedThread() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedByModerator).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(updatedThread.getTitle());
		}
		
		@Test
		void updateShouldSucceedIfDataIsValidAndModeratorHasRightsButModeratorDidNotCreateThread() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedBySomeoneElse).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(updatedThread.getTitle());
		}
		
		@Test
		void updateShouldFailIfTitleIsEmpty() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedBySomeoneElse).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle("");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfTitleIsTooLong() {
			ThreadDTO savedThread = threadService.findById(threadIdCreatedBySomeoneElse).orElseThrow();
			ThreadDTO updatedThread = savedThread.withTitle(Strings.repeat(" ",81));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfDataIsValidAndModeratorDoesNotHaveRights() {
			ThreadDTO savedThread = threadService.getAllByCategoryId(categoryIdWhereModDoesNotHaveRights,Pageable.ofSize(20)).getContent().get(0).getThread();
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedThread).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExistsAndModeratorHasRights() {
			ThreadDTO savedThread = threadService.getAllByCategoryId(categoryIdWhereModHasRights,Pageable.ofSize(20)).getContent().get(0).getThread();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExistsAndModeratorDoesNotHaveRights() {
			ThreadDTO savedThread = threadService.getAllByCategoryId(categoryIdWhereModDoesNotHaveRights,Pageable.ofSize(20)).getContent().get(0).getThread();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedThread.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
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
			Page<ThreadDTO> page = threadService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<ThreadDTO> threadProjDTOS = RestAssured
					.given()
					.when()
						.get(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().body().jsonPath().getList("content",ThreadDTO.class);
			//@formatter:on
			
			assertThat(threadProjDTOS).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnAllEntities() {
			Optional<ThreadDTO> byId = threadService.findById(1L);
			assertThat(byId).isNotEmpty();
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.THREADS_PATH+"/"+byId.get().getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(ThreadDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(byId.get());
		}
		
		@Test
		void createShouldSucceedIfDataIsValid() {
			ThreadCreateDTO threadCreateDTO = validThread;
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(threadCreateDTO.getTitle());
		}
		
		@Test
		void createShouldSucceedIfCategoryIsInactive() {
			ThreadCreateDTO threadCreateDTO = validThread.withCategoryId(inactiveCategoryId);
			
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(threadCreateDTO.getTitle());
		}
		
		@Test
		void createShouldFailIfTitleIsEmpty() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle("");
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfTitleIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withTitle(Strings.repeat(" ",81));
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",9));
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			ThreadCreateDTO threadCreateDTO = validThread.withContent(Strings.repeat(" ",10001));
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(threadCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.THREADS_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			ThreadDTO savedThread = threadService.getAll(Pageable.ofSize(20)).getContent().get(0);
			ThreadDTO updatedThread = savedThread.withTitle("Changed title");
			//@formatter:off
			ThreadDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedThread).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(ThreadDTO.class);
			//@formatter:on
			assertThat(result.getTitle()).isEqualTo(updatedThread.getTitle());
		}
		
		@Test
		void updateShouldFailIfTitleIsEmpty() {
			ThreadDTO savedThread = threadService.getAll(Pageable.ofSize(20)).getContent().get(0);
			ThreadDTO updatedThread = savedThread.withTitle("");
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedThread).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfTitleIsTooLong() {
			ThreadDTO savedThread = threadService.getAll(Pageable.ofSize(20)).getContent().get(0);
			ThreadDTO updatedThread = savedThread.withTitle(Strings.repeat(" ",81));
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedThread).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.THREADS_PATH+"/"+savedThread.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			ThreadDTO savedThread = threadService.getAll(Pageable.ofSize(20)).getContent().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.CATEGORIES_PATH+"/"+savedThread.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
		}
	}
}