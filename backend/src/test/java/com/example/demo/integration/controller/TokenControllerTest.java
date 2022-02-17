package com.example.demo.integration.controller;

import com.example.demo.categories.CategoryRepository;
import com.example.demo.categories.CategoryService;
import com.example.demo.posts.PostService;
import com.example.demo.roles.RoleRepository;
import com.example.demo.roles.RoleService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.ThreadRepository;
import com.example.demo.threads.ThreadService;
import com.example.demo.users.UserService;
import io.restassured.RestAssured;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class TokenControllerTest {
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
	
	private String token;
	
	void initData() {
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
		void refreshTokenShouldFail() {
			//@formatter:off
			RestAssured
					.given()
					.when()
					.get(SecurityUtility.TOKEN_REFRESH_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
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
		void refreshTokenShouldSucceed() {
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.TOKEN_REFRESH_PATH)
					.then()
					.statusCode(HttpStatus.OK.value());
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
		void refreshTokenShouldSucceed() {
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.TOKEN_REFRESH_PATH)
					.then()
					.statusCode(HttpStatus.OK.value());
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
		void refreshTokenShouldSucceed() {
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.TOKEN_REFRESH_PATH)
					.then()
					.statusCode(HttpStatus.OK.value());
			//@formatter:on
		}
	}
}