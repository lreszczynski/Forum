package com.example.demo.roles.validation;

import com.example.demo.roles.RoleDTO;
import com.example.demo.roles.RoleService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class RoleUniqueValidator implements ConstraintValidator<RoleUniqueConstraint, RoleDTO> {
	RoleService roleService;
	
	@Autowired
	public RoleUniqueValidator(RoleService roleService) {
		this.roleService = roleService;
	}
	
	public RoleUniqueValidator() {
	}
	
	private String message;
	
	public void initialize(RoleUniqueConstraint constraintAnnotation) {
		this.message = constraintAnnotation.message();
	}
	
	@Override
	public boolean isValid(RoleDTO roleDTO, ConstraintValidatorContext cxt) {
		cxt.disableDefaultConstraintViolation();
		cxt.buildConstraintViolationWithTemplate("Role name exists").addPropertyNode("name").addConstraintViolation();
		
		// repository.save(object) uses validators under the hood (if available), but those are unable to autowire repositories.
		// The @Valid annotation calls validator that is capable of autowiring services and repositories.
		if (roleService == null)
			return true;
		
		// update
		String nameValue = roleDTO.getName();
		if (roleDTO.getId() != null) {
			Optional<RoleDTO> optionalRoleDTO = roleService.getById(roleDTO.getId());
			if (optionalRoleDTO.isPresent() && !nameValue.equals(optionalRoleDTO.get().getName())) {
				// name changed
				return !roleService.existsRoleByNameAndIdIsNot(nameValue, roleDTO.getId());
			}
			return true;
		}
		// create
		return !roleService.existsRoleByName(nameValue);
	}
	
}