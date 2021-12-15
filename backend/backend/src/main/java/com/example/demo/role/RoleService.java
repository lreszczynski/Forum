package com.example.demo.role;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleService {
	final RoleRepository roleRepository;
	final ModelMapper modelMapper;
	
	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public List<RoleDTO> getAll() {
		Type listType = new TypeToken<List<RoleDTO>>() {
		}.getType();
		return modelMapper.map(roleRepository.findAll(), listType);
	}
	
	public Optional<RoleDTO> getById(Long id) {
		return Optional.of(modelMapper.map(roleRepository.findById(id), RoleDTO.class));
	}
	
	public RoleDTO create(RoleDTO roleDTO) {
		Role role = modelMapper.map(roleDTO, Role.class);
		Role saved = roleRepository.save(role);
		return modelMapper.map(saved, RoleDTO.class);
	}
	
	public void deleteById(Long id) {
		roleRepository.deleteById(id);
	}
	
	public Optional<RoleDTO> update(Long id, RoleDTO roleDTO) {
		if (!roleDTO.getId().equals(id)) {
			return Optional.empty();
		}
		Role role = modelMapper.map(roleDTO, Role.class);
		Role saved = roleRepository.save(role);
		
		return Optional.of(modelMapper.map(saved, RoleDTO.class));
	}
	
	public boolean existsRoleByName(String name) {
		return roleRepository.existsRoleByName(name);
	}
	
	public boolean existsRoleByNameAndIdIsNot(String name, Long id) {
		return roleRepository.existsRoleByNameAndIdIsNot(name, id);
	}
	
}
