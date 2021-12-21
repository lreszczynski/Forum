package com.example.demo;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryRepository;
import com.example.demo.categories.CategoryService;
import com.example.demo.roles.RoleRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
		CategoryService categoryService = (CategoryService) run.getBean("categoryService");
		CategoryRepository categoryRepository = (CategoryRepository) run.getBean("categoryRepository");
		RoleRepository roleRepository = (RoleRepository) run.getBean("roleRepository");
		
		categoryService.addRolesToCategoryByIds(CategoryDTO.builder().id(2L).build(),2L);
		//categoryService.addRolesToCategoryByIds(CategoryDTO.builder().id(1L).build(), 1000L);
		/*long start, finish, timeElapsed;
		
		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			System.out.println(roleRepository.findRoleByCategoryIdAndRoleId(2L, 1L));
			System.out.println(roleRepository.findRoleByCategoryIdAndRoleId(2L, 2L));
			System.out.println(roleRepository.findRoleByCategoryIdAndRoleId(2L, 3L));
		}
		finish = System.currentTimeMillis();
		timeElapsed = finish - start;
		
		System.out.println("1>>> " + timeElapsed);
		System.out.println("--------------------");*/
		
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
