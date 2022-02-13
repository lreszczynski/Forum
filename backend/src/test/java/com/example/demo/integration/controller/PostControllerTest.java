package com.example.demo.integration.controller;

import com.example.demo.categories.CategoryRepository;
import com.example.demo.categories.CategoryService;
import com.example.demo.posts.PostService;
import com.example.demo.posts.dto.PostCreateDTO;
import com.example.demo.posts.dto.PostDTO;
import com.example.demo.roles.RoleRepository;
import com.example.demo.roles.RoleService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.ThreadRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class PostControllerTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ThreadRepository threadRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PostService postService;
	@Autowired
	private ThreadService threadService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	
	private PostCreateDTO validPost;
	
	Long activeThreadIdInCategoryWhereEveryoneHasRights = 6L;
	Long activeThreadIdInCategoryWhereOnlyAdminHasRights = 1L;
	Long activeThreadIdInCategoryWhereOnlyModeratorHasRights = 2L;
	Long inactiveThreadIdInCategoryWhereEveryoneHasRights = 3L;
	
	private String token;
	
	void initData() {
		validPost = PostCreateDTO.builder().content("Long enough content")
				.threadId(activeThreadIdInCategoryWhereEveryoneHasRights).build();
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
	@DisplayName("If user is a GUEST")
	class GuestTests {
		@BeforeEach
		public void setUp() {
		
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<PostDTO> page = postService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<PostDTO> list = RestAssured
					.given()
					.when()
					.get(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",PostDTO.class);
			//@formatter:on
			
			assertThat(list).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			PostDTO postDTO = postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights,
					Pageable.unpaged()).getContent().get(0);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.when()
					.get(SecurityUtility.POSTS_PATH+"/"+postDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(postDTO);
		}
		
		@Test
		void createShouldReturnForbidden() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(activeThreadIdInCategoryWhereEveryoneHasRights);
			
			//@formatter:off
			RestAssured
					.given()
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldReturnForbidden() {
			PostDTO savedPost = postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights,
					Pageable.unpaged()).getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			RestAssured
					.given()
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldReturnForbidden() {
			PostDTO savedPost = postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights,
					Pageable.unpaged()).getContent().get(0);
			//@formatter:off
			RestAssured
					.given()
					.when()
					.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role USER")
	class UserTests {
		Long postIdCreatedByUser = 7L;
		Long postIdCreatedBySomeoneElse = 5L;
		
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<PostDTO> page = postService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<PostDTO> list = RestAssured
					.given()
					.when()
					.get(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",PostDTO.class);
			//@formatter:on
			
			assertThat(list).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			PostDTO postDTO = postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights,
					Pageable.unpaged()).getContent().get(0);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.POSTS_PATH+"/"+postDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(postDTO);
		}
		
		@Test
		void createShouldSucceedIfDataIsValidAndThreadIsActiveAndUserHasRights() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(activeThreadIdInCategoryWhereEveryoneHasRights);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.CREATED.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(postCreateDTO.getContent());
		}
		
		@Test
		void createShouldFailIfDataIsValidButUserDoesNotHaveRights() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsEmpty() {
			PostCreateDTO postCreateDTO = validPost.withContent("");
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 10001));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 9));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValidAndUserIsThePostCreator() {
			PostDTO savedPost = postService.getById(postIdCreatedByUser).orElseThrow();
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(updatedPost.getContent());
		}
		
		@Test
		void updateShouldFailIfDataIsValidButUserDidNotCreateThePost() {
			PostDTO savedPost = postService.getById(postIdCreatedBySomeoneElse).orElseThrow();
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsEmpty() {
			PostDTO savedPost = postService.getById(postIdCreatedByUser).orElseThrow();
			PostDTO updatedPost = savedPost.withContent("");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooShort() {
			PostDTO savedPost = postService.getById(postIdCreatedByUser).orElseThrow();
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 9));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooLong() {
			PostDTO savedPost = postService.getById(postIdCreatedByUser).orElseThrow();
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 10001));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfUserCreatedThePost() {
			PostDTO savedPost = postService.getById(postIdCreatedByUser).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
			assertThat(postService.getById(savedPost.getId())).isEmpty();
		}
		
		@Test
		void deleteShouldFailIfUserDidNotCreateThePost() {
			PostDTO savedPost = postService.getById(postIdCreatedBySomeoneElse).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
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
			Page<PostDTO> page = postService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<PostDTO> list = RestAssured
					.given()
					.when()
					.get(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().body().jsonPath().getList("content",PostDTO.class);
			//@formatter:on
			
			assertThat(list).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			PostDTO postDTO = postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights,
					Pageable.unpaged()).getContent().get(0);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.POSTS_PATH+"/"+postDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(postDTO);
		}
		
		@Test
		void createShouldSucceedIfDataIsValidAndThreadIsActiveAndModeratorHasRights() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(activeThreadIdInCategoryWhereEveryoneHasRights);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.CREATED.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(postCreateDTO.getContent());
		}
		
		@Test
		void createShouldFailIfDataIsValidButModeratorDoesNotHaveRights() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsEmpty() {
			PostCreateDTO postCreateDTO = validPost.withContent("");
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 10001));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 9));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValidAndModeratorHasRights() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(updatedPost.getContent());
		}
		
		@Test
		void updateShouldFailIfDataIsValidButModeratorDoesNotHaveRights() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsEmpty() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooShort() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 9));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooLong() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 10001));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExistsAndModeratorHasRights() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereEveryoneHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
			assertThat(postService.getById(savedPost.getId())).isEmpty();
		}
		
		@Test
		void deleteShouldFailIfEntityExistsButModeratorDoesNotHaveRights() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
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
			Page<PostDTO> page = postService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<PostDTO> list = RestAssured
					.given()
					.when()
						.get(SecurityUtility.POSTS_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().body().jsonPath().getList("content",PostDTO.class);
			//@formatter:on
			
			assertThat(list).isEqualTo(page.getContent());
		}
		
		@Test
		void getByIdShouldReturnEntityIfItExists() {
			PostDTO postDTO = postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyModeratorHasRights,
					Pageable.unpaged()).getContent().get(0);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.POSTS_PATH+"/"+postDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(postDTO);
		}
		
		@Test
		void createShouldSucceedIfDataIsValid() {
			PostCreateDTO postCreateDTO = validPost;
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.POSTS_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(postCreateDTO.getContent());
		}
		
		@Test
		void createShouldSucceedIfThreadIsInactive() {
			PostCreateDTO postCreateDTO = validPost.withThreadId(inactiveThreadIdInCategoryWhereEveryoneHasRights);
			
			//@formatter:off
			PostDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.CREATED.value())
					.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(postCreateDTO.getContent());
		}
		
		@Test
		void createShouldFailIfContentIsEmpty() {
			PostCreateDTO postCreateDTO = validPost.withContent("");
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooLong() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 10001));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFailIfContentIsTooShort() {
			PostCreateDTO postCreateDTO = validPost.withContent(Strings.repeat(" ", 9));
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(postCreateDTO).contentType(ContentType.JSON)
					.when()
					.post(SecurityUtility.POSTS_PATH)
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("Changed content");
			//@formatter:off
			PostDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updatedPost).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(PostDTO.class);
			//@formatter:on
			assertThat(result.getContent()).isEqualTo(updatedPost.getContent());
		}
		
		@Test
		void updateShouldFailIfContentIsEmpty() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent("");
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooShort() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 9));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfContentIsTooLong() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			PostDTO updatedPost = savedPost.withContent(Strings.repeat(" ", 10001));
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updatedPost).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			PostDTO savedPost =
					postService.getAllByThreadId(activeThreadIdInCategoryWhereOnlyAdminHasRights, Pageable.ofSize(20))
							.getContent().get(0);
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.POSTS_PATH+"/"+savedPost.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
			assertThat(postService.getById(savedPost.getId())).isEmpty();
		}
	}
}