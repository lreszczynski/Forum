package com.example.demo.categories.validation;

import com.example.demo.categories.CategoryDTO;
import com.example.demo.categories.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class CategoryUniqueValidator implements ConstraintValidator<CategoryUniqueConstraint, CategoryDTO> {
	CategoryService categoryService;
	
	@Autowired
	public CategoryUniqueValidator(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	public CategoryUniqueValidator() {
	}
	
	private String message;
	
	public void initialize(CategoryUniqueConstraint constraintAnnotation) {
		this.message = constraintAnnotation.message();
	}
	
	@Override
	public boolean isValid(CategoryDTO categoryDTO, ConstraintValidatorContext cxt) {
		cxt.disableDefaultConstraintViolation();
		cxt.buildConstraintViolationWithTemplate("Category name exists").addPropertyNode("name").addConstraintViolation();
		
		// repository.save(object) uses validators under the hood (if available), but those are unable to autowire repositories.
		// The @Valid annotation calls validator that is capable of autowiring services and repositories.
		if (categoryService == null)
			return true;
		
		// update
		String nameValue = categoryDTO.getName();
		if (categoryDTO.getId() != null) {
			Optional<CategoryDTO> optionalCategoryDTO = categoryService.getById(categoryDTO.getId());
			if (optionalCategoryDTO.isPresent() && !nameValue.equals(optionalCategoryDTO.get().getName())) {
				// name changed
				return !categoryService.existsCategoryByNameAndIdIsNot(nameValue, categoryDTO.getId());
			}
			return true;
		}
		// create
		return !categoryService.existsCategoryByName(nameValue);
	}
	
}