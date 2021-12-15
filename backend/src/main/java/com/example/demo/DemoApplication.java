package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
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
