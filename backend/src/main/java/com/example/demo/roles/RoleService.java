package com.example.demo.roles;

import com.example.demo.roles.dto.RoleDTO;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class RoleService {
	private final RoleRepository roleRepository;
	private final ModelMapper modelMapper;
	
	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
		this.modelMapper = new ModelMapper();
	}
	
	public Page<RoleDTO> getAll(Pageable pageable) {
		Page<Role> page = roleRepository.findAll(pageable);
		return page.map(user -> modelMapper.map(user, RoleDTO.class));
	}
	
	public Optional<RoleDTO> getById(Long id) {
		Optional<Role> optionalRole = roleRepository.findById(id);
		return optionalRole.map(role -> modelMapper.map(role, RoleDTO.class));
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
	
	public Optional<RoleDTO> findByName(String name) {
		return roleRepository.findByName(name).map(role -> modelMapper.map(role, RoleDTO.class));
	}
	
	public boolean existsRoleByName(String name) {
		return roleRepository.existsRoleByName(name);
	}
	
	public boolean existsRoleByNameAndIdIsNot(String name, Long id) {
		return roleRepository.existsRoleByNameAndIdIsNot(name, id);
	}
	
}
