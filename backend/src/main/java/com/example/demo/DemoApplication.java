package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
		
		Path path = Paths.get(System.getProperty("user.dir")).getParent().resolve("database-scripts");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.sql");
		System.out.println(path.toAbsolutePath());
		try (Stream<Path> paths = Files.walk(Paths.get(path.toString()))) {
			paths.filter(matcher::matches).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//File projectDir = new File(System.getProperty("user.dir"));
		//File databaseFiles =projectDir.getParentFile().listFiles((file, s) -> );
		//System.out.println();
		/*CategoryRepository categoryRepository = (CategoryRepository) run.getBean("categoryRepository");
		RoleRepository roleRepository = (RoleRepository) run.getBean("roleRepository");
		CategoryService categoryService = (CategoryService) run.getBean("categoryService");
		RoleService roleService = (RoleService) run.getBean("roleService");
		
		//System.out.println(categoryService.getRoles(categoryService.getById(1L).get()));
		
		System.out.println(">>> CategoryRepo:");
		System.out.println(categoryRepository.findAll().stream().map(Category::getName).collect(Collectors.toList()));
		//System.out.println(roleRepository.getRolesByCategoryId(1L));
		System.out.println(">>> Role:");
		System.out.println(roleRepository.getRolesByCategoryId(1L));
		//System.out.println(roleRepository.getRolesByCategoryId(2L));
		Optional<CategoryDTO> byId = categoryService.getById(1L);
		byId.ifPresent(categoryDTO -> categoryService.addRolesToCategory(categoryDTO, roleService.getById(1L).get()));
		System.out.println(">>> Role1:");
		System.out.println(roleRepository.getRolesByCategoryId(1L));
		byId.ifPresent(categoryDTO -> categoryService.deleteRolesFromCategory(categoryDTO, roleService.getById(1L).get()));
		System.out.println(">>> Role2:");
		System.out.println(roleRepository.getRolesByCategoryId(1L));
		//System.out.println(categoryService.getRoles(categoryRepository.getById(1L)));
		//System.out.println(categoryRepository.getById(1L).getRoles());*/
	}
	
}
