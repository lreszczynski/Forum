package com.example.demo.categories;

import com.example.demo.categoryroles.CategoryRoleRepository;
import com.example.demo.posts.Post;
import com.example.demo.posts.PostRepository;
import com.example.demo.roles.Role;
import com.example.demo.roles.RoleDTO;
import com.example.demo.roles.RoleRepository;
import com.example.demo.threads.Thread;
import com.example.demo.threads.ThreadRepository;
import com.example.demo.threads.ThreadWithLastPostDTO;
import com.example.demo.users.UserRepository;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final RoleRepository roleRepository;
	private final ModelMapper modelMapper;
	
	public CategoryService(CategoryRepository categoryRepository, RoleRepository roleRepository, PostRepository postRepository, ThreadRepository threadRepository, CategoryRoleRepository categoryRoleRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.roleRepository = roleRepository;
		this.modelMapper = new ModelMapper();
		TypeMap<Thread, ThreadWithLastPostDTO> propertyMapper = modelMapper.createTypeMap(Thread.class, ThreadWithLastPostDTO.class);
		Converter<Collection<Post>, Integer> collectionToSize = (c -> c.getSource().size());
		propertyMapper.addMappings(mapper -> mapper.using(collectionToSize).map(Thread::getPosts, ThreadWithLastPostDTO::setPostsCount));
	}
	
	public List<CategoryDTO> findAll() {
		return modelMapper.map(categoryRepository.findAll(), new TypeToken<List<CategoryDTO>>() {
		}.getType());
	}
	
	public Optional<CategoryDTO> findById(Long id) {
		return categoryRepository.findById(id).map(category -> modelMapper.map(category, CategoryDTO.class));
	}
	
	public CategoryDTO create(CategoryDTO categoryDTO) {
		Category category = Category.builder()
				.name(categoryDTO.getName())
				.description(categoryDTO.getDescription())
				.active(categoryDTO.isActive())
				.roles(categoryDTO.getRoles().stream().map(roleDTO -> modelMapper.map(roleDTO, Role.class)).collect(
						Collectors.toSet()))
				.build();
		
		Category saved = categoryRepository.save(category);
		
		return modelMapper.map(saved, CategoryDTO.class);
	}
	
	public void deleteById(Long id) {
		categoryRepository.deleteById(id);
	}
	
	public Optional<CategoryDTO> update(Long id, CategoryDTO categoryDTO) {
		Optional<Category> optionalCategory = categoryRepository.findById(id).map(category -> {
			category.setName(categoryDTO.getName());
			category.setDescription(categoryDTO.getDescription());
			category.setActive(categoryDTO.isActive());
			Set<Role> newRoles = new HashSet<>();
			categoryDTO.getRoles().forEach(roleDTO -> {
				Role byId = roleRepository.getById(roleDTO.getId());
				newRoles.add(byId);
			});
			category.setRoles(newRoles);
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
			for (Long id : ids) {
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
			for (Long id : ids) {
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
