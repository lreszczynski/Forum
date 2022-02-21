package com.example.demo.integration.controller;

import com.example.demo.roles.RoleService;
import com.example.demo.roles.dto.RoleDTO;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
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
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class RoleControllerTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private RoleService roleService;
	
	Long roleUserId;
	Long roleModId;
	Long roleAdminId;
	
	private String token;
	
	void initData() {
		roleUserId = roleService.findByName("User").orElseThrow().getId();
		roleModId = roleService.findByName("Moderator").orElseThrow().getId();
		roleAdminId = roleService.findByName("Admin").orElseThrow().getId();
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
			//@formatter:off
			RestAssured
					.given()
					.when()
						.get(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void getByIdShouldSucceed() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			
			//@formatter:off
			RestAssured
					.given()
					.when()
						.get(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void createShouldFail() {
			RoleDTO roleDTO = RoleDTO.builder().name("New role").description("description").build();
			
			//@formatter:off
			RestAssured
					.given()
						.body(roleDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFail() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withDescription("description").withName("Role name");
			
			//@formatter:off
			RestAssured
					.given()
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
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
			List<RoleDTO> roleDTOList = roleService.getAll();
			
			//@formatter:off
			List<RoleDTO> roleDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO[].class));
			//@formatter:on
			
			assertThat(roleDTOS).isEqualTo(roleDTOList);
		}
		
		@Test
		void getByIdShouldSucceed() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			
			//@formatter:off
			RoleDTO result = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(roleDTO);
		}
		
		@Test
		void createShouldFail() {
			RoleDTO roleDTO = RoleDTO.builder().name("New role").description("description").build();
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(roleDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFail() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withDescription("description").withName("Role name");
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
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
			List<RoleDTO> roleDTOList = roleService.getAll();
			
			//@formatter:off
			List<RoleDTO> roleDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO[].class));
			//@formatter:on
			
			assertThat(roleDTOS).isEqualTo(roleDTOList);
		}
		
		@Test
		void getByIdShouldSucceed() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			
			//@formatter:off
			RoleDTO result = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(roleDTO);
		}
		
		@Test
		void createShouldFail() {
			RoleDTO roleDTO = RoleDTO.builder().name("New role").description("description").build();
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(roleDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFail() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withDescription("description").withName("Role name");
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
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
			List<RoleDTO> roleDTOList = roleService.getAll();
			
			//@formatter:off
			List<RoleDTO> roleDTOS = List.of(RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO[].class));
			//@formatter:on
			
			assertThat(roleDTOS).isEqualTo(roleDTOList);
		}
		
		@Test
		void getByIdShouldSucceed() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			
			//@formatter:off
			RoleDTO result = RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.get(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(roleDTO);
		}
		
		@Test
		void createShouldSucceedIfRoleNameIsUnique() {
			RoleDTO roleDTO = RoleDTO.builder().name("New role").description("description").build();
			
			//@formatter:off
			RoleDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(roleDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.CREATED.value())
						.extract().as(RoleDTO.class);
			//@formatter:on
			
			assertThat(result.getName()).isEqualTo(roleDTO.getName());
		}
		
		@Test
		void createShouldFailIfRoleNameIsNotUnique() {
			RoleDTO roleDTO = RoleDTO.builder().name("User").description("description").build();
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(roleDTO).contentType(ContentType.JSON)
					.when()
						.post(SecurityUtility.ROLES_PATH)
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withDescription("description").withName("Role name");
			
			//@formatter:off
			RoleDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(RoleDTO.class);
			//@formatter:on
			assertThat(result).isEqualTo(updated);
		}
		
		@Test
		void updateShouldFailIfDescriptionIsNull() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withDescription(null);
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsTooShort() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withName("");
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFailIfNameIsTooLong() {
			RoleDTO roleDTO = roleService.getById(roleAdminId).orElseThrow();
			RoleDTO updated = roleDTO.withName(Strings.repeat(" ", 51));
			
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.ROLES_PATH+"/"+roleDTO.getId())
					.then()
						.statusCode(HttpStatus.BAD_REQUEST.value());
			//@formatter:on
		}
	}
}