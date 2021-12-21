package com.example.demo.categories;

import com.example.demo.categoryroles.CategoryRoleRepository;
import com.example.demo.roles.Role;
import com.example.demo.roles.RoleDTO;
import com.example.demo.roles.RoleRepository;
import com.example.demo.users.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final RoleRepository roleRepository;
	private final CategoryRoleRepository categoryRoleRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	
	public CategoryService(CategoryRepository categoryRepository, RoleRepository roleRepository, CategoryRoleRepository categoryRoleRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.roleRepository = roleRepository;
		this.categoryRoleRepository = categoryRoleRepository;
		this.userRepository = userRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<CategoryDTO> findAll() {
		return modelMapper.map(categoryRepository.findAll(), new TypeToken<List<CategoryDTO>>() {
		}.getType());
	}
	
	public Optional<CategoryDTO> findById(Long id) {
		return categoryRepository.findById(id).map(category -> modelMapper.map(category, CategoryDTO.class));
	}
	
	public CategoryDTO create(CategoryDTO categoryDTO, String username) {
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category saved = categoryRepository.save(category);
		CategoryDTO savedDto = modelMapper.map(saved, CategoryDTO.class);
		userRepository.findByUsername(username).ifPresent(user -> addRolesToCategoryByIds(savedDto, user.getRole().getId()));
		return savedDto;
	}
	
	public void deleteById(Long id) {
		categoryRepository.deleteById(id);
	}
	
	public Optional<CategoryDTO> update(Long id, CategoryDTO categoryDTO) {
		Optional<Category> optionalCategory = categoryRepository.findById(id).map(category -> {
			category.setName(categoryDTO.getName());
			category.setDescription(categoryDTO.getDescription());
			category.setActive(categoryDTO.isActive());
			return categoryRepository.save(category);
		});
		return optionalCategory.map(category -> modelMapper.map(category, CategoryDTO.class));
	}
	
	public Optional<Set<RoleDTO>> findRolesForCategory(CategoryDTO categoryDTO) {
		return findRolesForCategoryById(categoryDTO.getId());
	}
	
	public Optional<Set<RoleDTO>> findRolesForCategoryById(Long id) {
		return categoryRepository.findById(id).map(category -> {
			Set<Role> roles = category.getRoles();
			Type listType = new TypeToken<Set<RoleDTO>>() {
			}.getType();
			return modelMapper.map(roles, listType);
		});
	}
	
	public void addRolesToCategory(CategoryDTO categoryDTO, RoleDTO... roles) {
		addRolesToCategoryByIds(categoryDTO, Arrays.stream(roles).map(RoleDTO::getId).toArray(Long[]::new));
	}
	
	public void addRolesToCategoryByIds(CategoryDTO categoryDTO, Long... ids) {
		Optional<Category> optionalCategory = categoryRepository.findById(categoryDTO.getId());
		optionalCategory.ifPresent(category -> {
			for (Long id: ids) {
				Optional<Role> byId = roleRepository.findById(id);
				byId.ifPresent(role -> category.getRoles().add(role));
			}
		});
	}
	
	public void deleteRolesFromCategory(CategoryDTO categoryDTO, RoleDTO... roles) {
		deleteRolesFromCategoryByIds(categoryDTO, Arrays.stream(roles).map(RoleDTO::getId).toArray(Long[]::new));
	}
	
	public void deleteRolesFromCategoryByIds(CategoryDTO categoryDTO, Long... ids) {
		Optional<Category> optionalCategory = categoryRepository.findById(categoryDTO.getId());
		optionalCategory.ifPresent(category -> {
			for (Long id: ids) {
				Optional<Role> byId = roleRepository.findById(id);
				byId.ifPresent(role -> category.getRoles().remove(role));
				//categoryRoleRepository.deleteById(CategoryRolePK.builder().categoryId(category.getId()).roleId(id).build());
			}
		});
	}
	
	public boolean existsCategoryByName(String name) {
		return categoryRepository.existsCategoryByName(name);
	}
	
	public boolean existsCategoryByNameAndIdIsNot(String name, Long id) {
		return categoryRepository.existsCategoryByNameAndIdIsNot(name, id);
	}
}
