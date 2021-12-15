package com.example.demo.integration.controller;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
//@Sql({"classpath:database-scripts/delete.sql", "classpath:database-scripts/create.sql"})
class CategoryControllerTest {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(CategoryControllerTest.class);
	
	@Autowired
	private CategoryRepository repository;
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	private JacksonTester<Category> jacksonTester;
	
	Category[] categories;
	
	@BeforeEach
	public void setUp() {
		System.out.println("setUp");
		JacksonTester.initFields(this, new ObjectMapper());
		categories = new Category[]{
				Category.builder().name("Announcements").description("description").active(true).build(),
				Category.builder().name("General").description("description").active(true).build()
		};
		repository.saveAll(Arrays.asList(categories));
	}
	
	@AfterEach
	void tearDown() {
		System.out.println("tearDown");
		repository.deleteAll(Arrays.asList(categories));
	}
	
	@Test
	void testFindAll() {
		ResponseEntity<Category[]> response =
				restTemplate.getForEntity("/categories/", Category[].class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(categories);
	}
	
	@Test
	void createShouldFailGivenNotUniqueData() {
		Category category = Category.builder().name("Announcements").description("de").active(true).build();
		
		ResponseEntity<Object> response =
				restTemplate.postForEntity("/categories/", category, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo("{test2=test2, category=Test}");
	}
	
	/*@Test
	void createShouldFailGivenNotUniqueData() {
		Category category = Category.builder()
				.name("Announcements")
				.description("description")
				.active(true)
				.build();
		ResponseEntity<Category> response = categoryController.create(category);
		Assertions.assertThat(Objects.requireNonNull(response.getBody())).isEqualTo(categoryRepository.getById(category.getId()));
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}*/
	
	/*@Test
	void findAllShouldReturnWholeCollection() {
		long count = categoryRepository.count();
		ResponseEntity<Collection<Category>> response = categoryController.findAll();
		Assertions.assertThat(response.getBody()).isInstanceOf(Collection.class);
		Assertions.assertThat(Objects.requireNonNull(response.getBody()).size()).isEqualTo(count);
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	void findByIdShouldReturnEntityIfItExists() {
		ResponseEntity<Category> response = categoryController.findById(category1.getId());
		Assertions.assertThat(response.getBody()).isEqualTo(category1);
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	void findByIdShouldReturnNotFoundIfEntityDoesNotExist() {
		ResponseEntity<Category> response = categoryController.findById(Long.MAX_VALUE);
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	void createShouldSucceedGivenValidData() {
		Category category = Category.builder()
				.name("Unique name")
				.description("description")
				.active(true)
				.build();
		ResponseEntity<Category> response = categoryController.create(category);
		Assertions.assertThat(Objects.requireNonNull(response.getBody())).isEqualTo(categoryRepository.getById(category.getId()));
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}*/
	
	@Test
	void delete() {
	}
	
	@Test
	void update() {
	}
}