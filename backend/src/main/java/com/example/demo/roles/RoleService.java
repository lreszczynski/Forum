package com.example.demo.roles;

import com.example.demo.roles.dto.RoleDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
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
	
	public List<RoleDTO> getAll() {
		return modelMapper.map(roleRepository.findAll(), new TypeToken<List<RoleDTO>>() {
		}.getType());
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
	
	/*public boolean deleteById(Long id) {
		Optional<Role> roleDTO = roleRepository.findById(id);
		if (roleDTO.isPresent()) {
			String name = roleDTO.get().getName().toUpperCase();
			if (Arrays.asList(RoleContainer.ADMIN, RoleContainer.MODERATOR, RoleContainer.USER).contains(name)) {
				return false;
			}
			else {
				roleRepository.deleteById(id);
				return true;
			}
		}
		return false;
	}*/
	
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
