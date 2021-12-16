package com.example.demo.categories;

import com.example.demo.roles.Role;
import com.example.demo.roles.RoleDTO;
import com.example.demo.roles.RoleRepository;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;
import org.hibernate.Hibernate;
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
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	
	public CategoryService(CategoryRepository categoryRepository, RoleRepository roleRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<CategoryDTO> getAll() {
		Type listType = new TypeToken<List<CategoryDTO>>() {
		}.getType();
		return modelMapper.map(categoryRepository.findAll(), listType);
	}
	
	public Optional<CategoryDTO> getById(Long id) {
		Optional<Category> optionalCategory = categoryRepository.findById(id);
		return optionalCategory.map(category -> modelMapper.map(category, CategoryDTO.class));
	}
	
	public CategoryDTO create(CategoryDTO categoryDTO, String username) {
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category saved = categoryRepository.save(category);
		CategoryDTO savedDto = modelMapper.map(saved, CategoryDTO.class);
		Optional<User> optionalUser = userRepository.findByUsername(username);
		optionalUser.ifPresent(user -> addRolesToCategoryByIds(savedDto, user.getRole().getId()));
		
		return savedDto;
	}
	
	public void deleteById(Long id) {
		categoryRepository.deleteById(id);
	}
	
	public Optional<CategoryDTO> update(Long id, CategoryDTO categoryDTO) {
		if (!categoryDTO.getId().equals(id)) {
			return Optional.empty();
		}
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category saved = categoryRepository.save(category);
		
		return Optional.of(modelMapper.map(saved, CategoryDTO.class));
	}
	
	public boolean existsCategoryByName(String name) {
		return categoryRepository.existsCategoryByName(name);
	}
	
	public boolean existsCategoryByNameAndIdIsNot(String name, Long id) {
		return categoryRepository.existsCategoryByNameAndIdIsNot(name, id);
	}
	
	public Set<RoleDTO> getRolesForCategory(CategoryDTO categoryDTO) {
		return getRolesForCategoryById(categoryDTO.getId());
	}
	
	public Set<RoleDTO> getRolesForCategoryById(Long id) {
		Category byId = categoryRepository.getById(id);
		Hibernate.initialize(byId.getRoles());
		Set<Role> roles = byId.getRoles();
		Type listType = new TypeToken<Set<RoleDTO>>() {
		}.getType();
		return modelMapper.map(roles, listType);
	}
	
	public void addRolesToCategory(CategoryDTO categoryDTO, RoleDTO... roles) {
		addRolesToCategoryByIds(categoryDTO, Arrays.stream(roles).map(RoleDTO::getId).toArray(Long[]::new));
	}
	
	public void addRolesToCategoryByIds(CategoryDTO categoryDTO, Long... ids) {
		Category byId = categoryRepository.getById(categoryDTO.getId());
		for (Long id : ids) {
			byId.getRoles().add(roleRepository.getById(id));
		}
		categoryRepository.save(byId);
	}
	
	public void deleteRolesFromCategory(CategoryDTO categoryDTO, RoleDTO... roles) {
		Category byId = categoryRepository.getById(categoryDTO.getId());
		for (RoleDTO roleDTO : roles) {
			byId.getRoles().remove(roleRepository.getById(roleDTO.getId()));
		}
		categoryRepository.save(byId);
	}
	
}
