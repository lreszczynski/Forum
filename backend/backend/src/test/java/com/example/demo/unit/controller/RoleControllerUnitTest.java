package com.example.demo.unit.controller;

import com.example.demo.role.RoleController;
import com.example.demo.role.RoleDTO;
import com.example.demo.role.RoleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

//@ExtendWith(SpringExtension.class)
@WebMvcTest(RoleController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureJsonTesters
class RoleControllerUnitTest {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(RoleControllerUnitTest.class);
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private RoleService service;
	
	private JacksonTester<RoleDTO> jacksonTester;
	private JacksonTester<RoleDTO[]> jacksonTesterArray;
	
	@BeforeAll
	void setUp() {
		JacksonTester.initFields(this, new ObjectMapper());
	}
	
	@AfterAll
	void tearDown() {
	}
	
	@Test
	void getAllShouldReturnAllEntities() throws Exception {
		// given
		RoleDTO[] roles = new RoleDTO[]{
				RoleDTO.builder().id(1L).name("Announcements").description("description").build(),
				RoleDTO.builder().id(2L).name("General").description("description").build()
		};
		given(service.getAll())
				.willReturn(List.of(roles));
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/roles/")
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(
				jacksonTesterArray.write(roles).getJson()
		);
	}
	
	@Test
	void getByIdShouldReturnEntityIfItExists() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.getById(role.getId()))
				.willReturn(Optional.of(role));
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/roles/1")
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(
				jacksonTester.write(role).getJson()
		);
	}
	
	@Test
	void getByIdShouldReturnNotFoundIfEntityDoesNotExist() throws Exception {
		// given
		given(service.getById(1L))
				.willReturn(Optional.empty());
		
		// when
		MockHttpServletResponse response = mvc.perform(
						get("/roles/1")
								.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getContentAsString()).isEmpty();
	}
	
	@Test
	void createShouldSucceedIfDataIsValid() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
	}
	
	@Test
	void createShouldFailIfDataIsNotUnique() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(true);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfIdIsNotNull() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsNull() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name(null)
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsEmpty() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfNameIsTooLong() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsNull() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("Announcements")
				.description(null)
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void createShouldFailIfDescriptionIsTooLong() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.name("Announcements")
				.description(StringUtils.repeat(' ', 251))
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				post("/roles/").contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	//update
	@Test
	void updateShouldSucceedIfDataIsValid() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("Unique")
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}
	
	@Test
	void updateShouldFailIfIdIsNull() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(null)
				.name("Announcements")
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsNotUnique() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("Not unique")
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(true);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsNull() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name(null)
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsEmpty() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("")
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfNameIsTooLong() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name(StringUtils.repeat(' ', 51))
				.description("description")
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsNull() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description(null)
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void updateShouldFailIfDescriptionIsTooLong() throws Exception {
		// given
		RoleDTO updatedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description(StringUtils.repeat(' ', 251))
				.build();
		RoleDTO savedRole = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByNameAndIdIsNot(updatedRole.getName(), updatedRole.getId()))
				.willReturn(false);
		given(service.getById(updatedRole.getId()))
				.willReturn(Optional.of(savedRole));
		given(service.update(eq(updatedRole.getId()), any(RoleDTO.class)))
				.willReturn(Optional.of(updatedRole));
		
		// when
		MockHttpServletResponse response = mvc.perform(
				put("/roles/" + updatedRole.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(updatedRole).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
	
	@Test
	void deleteShouldSucceedIfEntityExists() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(true);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				delete("/roles/" + role.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	void deleteShouldSucceedIfEntityDoesNotExist() throws Exception {
		// given
		RoleDTO role = RoleDTO.builder()
				.id(1L)
				.name("Announcements")
				.description("description")
				.build();
		given(service.existsRoleByName(role.getName()))
				.willReturn(false);
		
		// when
		MockHttpServletResponse response = mvc.perform(
				delete("/roles/" + role.getId()).contentType(MediaType.APPLICATION_JSON).content(
						jacksonTester.write(role).getJson()
				)).andReturn().getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
}