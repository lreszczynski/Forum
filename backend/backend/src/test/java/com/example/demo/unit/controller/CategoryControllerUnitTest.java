package com.example.demo.unit.controller;

import com.example.demo.TestUserDetailsService;
import com.example.demo.category.CategoryController;
import com.example.demo.category.CategoryDTO;
import com.example.demo.category.CategoryService;
import com.example.demo.security.RoleContainer;
import com.example.demo.utility.JWTUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

//@ExtendWith(SpringExtension.class)
@WebMvcTest(CategoryController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureJsonTesters
@Import(TestUserDetailsService.class)
class CategoryControllerUnitTest {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(CategoryControllerUnitTest.class);
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private CategoryService service;
	
	@MockBean(name = "Roles")
	private RoleContainer roleContainer;
	
	private JacksonTester<CategoryDTO> jacksonTester;
	private JacksonTester<CategoryDTO[]> jacksonTesterArray;
	
	String jwtToken;
	
	@BeforeAll
	void setUp() throws Exception {
		JacksonTester.initFields(this, new ObjectMapper());
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleContainer.USER));
		jwtToken = JWTUtility.generateToken("/", "user", authorities, JWTUtility.TOKEN_TYPE.ACCESS);
		given(roleContainer.isAtLeastModerator(any()))
				.willReturn(false);
	}
	
	@AfterAll
	void tearDown() {
	}
	
	@Test
		//@WithUserDetails("User")
	void getAllShouldReturnAllEntities() throws Exception {
		// given
		CategoryDTO[] categories = new CategoryDTO[]{
				CategoryDTO.builder().id(1L).name("Announcements").description("description").active(true).build(),
				CategoryDTO.builder().id(2L).name("General").description("description").active(true).build()
		};
		given(service.getAll())
				.willReturn(List.of(categories));
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/categories/")
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(
				jacksonTesterArray.write(categories).getJson()
		);
	}
	
	@Test
		//@WithUserDetails("Moderator")
	void getByIdShouldReturnEntityIfItExists() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.getById(category.getId()))
				.willReturn(Optional.of(category));
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/categories/1")
								.header(HttpHeaders.AUTHORIZATION, "Bearer "+jwtToken)
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(
				jacksonTester.write(category).getJson()
		);
	}
	
	@Test
	void getByIdShouldReturnNotFoundIfEntityDoesNotExist() throws Exception {
		// given
		given(service.getById(1L))
				.willReturn(Optional.empty());
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/categories/1")
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getContentAsString()).isEmpty();
	}
	
	@Test
	void createShouldSucceedIfDataIsValid() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
	}
	
	@Test
	void createShouldFailIfDataIsNotUnique() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(true);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfIdIsNotNull() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsNull() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name(null)
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsEmpty() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsTooLong() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name(StringUtils.repeat(' ', 51))
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsNull() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description(null)
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsEmpty() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description("")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsTooShort() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description(StringUtils.repeat(' ', 4))
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsTooLong() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.name("Announcements")
				.description(StringUtils.repeat(' ', 251))
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/categories/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	//update
	@Test
	void updateShouldSucceedIfDataIsValid() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Unique")
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	void updateShouldFailIfIdIsNull() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(null)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsNotUnique() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Not unique")
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(true);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsNull() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name(null)
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsEmpty() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("")
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsTooLong() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name(StringUtils.repeat(' ', 51))
				.description("description")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsNull() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description(null)
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsEmpty() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("")
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsTooShort() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description(StringUtils.repeat(' ', 4))
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsTooLong() throws Exception {
		// given
		CategoryDTO updatedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description(StringUtils.repeat(' ', 251))
				.active(true)
				.build();
		CategoryDTO savedCategory = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByNameAndIdIsNot(updatedCategory.getName(), updatedCategory.getId()))
				.willReturn(false);
		given(service.getById(updatedCategory.getId()))
				.willReturn(Optional.of(savedCategory));
		given(service.update(eq(updatedCategory.getId()), any(CategoryDTO.class)))
				.willReturn(Optional.of(updatedCategory));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/categories/" + updatedCategory.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedCategory).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void deleteShouldSucceedIfEntityExists() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(true);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				delete("/categories/" + category.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	void deleteShouldSucceedIfEntityDoesNotExist() throws Exception {
		// given
		CategoryDTO category = CategoryDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		given(service.existsCategoryByName(category.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				delete("/categories/" + category.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(category).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
}