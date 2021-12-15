package com.example.demo.category;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
	final CategoryRepository categoryRepository;
	final ModelMapper modelMapper;
	
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<CategoryDTO> getAll() {
		Type listType = new TypeToken<List<CategoryDTO>>() {
		}.getType();
		return modelMapper.map(categoryRepository.findAll(), listType);
	}
	
	public Optional<CategoryDTO> getById(Long id) {
		Optional<Category> byId = categoryRepository.findById(id);
		if (byId.isPresent()) {
			CategoryDTO map = modelMapper.map(byId.get(), CategoryDTO.class);
			return Optional.of(map);
		}
		return Optional.empty();
	}
	
	public CategoryDTO create(CategoryDTO categoryDTO) {
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category saved = categoryRepository.save(category);
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
	
}
