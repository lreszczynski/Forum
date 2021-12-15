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
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isPresent()) {
			addRolesToCategoryByIds(modelMapper.map(saved, CategoryDTO.class), optionalUser.get().getRole().getId());
		}
		
		return modelMapper.map(saved, CategoryDTO.class);
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
	
	public Set<Role> getRolesForCategory(CategoryDTO categoryDTO) {
		return getRolesForCategoryById(categoryDTO.getId());
	}
	public Set<Role> getRolesForCategoryById(Long id) {
		Category byId = categoryRepository.getById(id);
		Hibernate.initialize(byId.getRoles());
		return byId.getRoles();
	}
	
	public void addRolesToCategory(CategoryDTO categoryDTO, RoleDTO... roles) {
		Category byId = categoryRepository.getById(categoryDTO.getId());
		for (RoleDTO roleDTO : roles) {
			byId.getRoles().add(roleRepository.getById(roleDTO.getId()));
		}
		categoryRepository.save(byId);
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
