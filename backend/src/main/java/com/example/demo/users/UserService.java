package com.example.demo.users;

import com.example.demo.roles.RoleRepository;
import com.example.demo.security.RoleContainer;
import com.example.demo.users.dto.UserDTO;
import com.example.demo.users.dto.UserProfileDTO;
import com.example.demo.users.dto.UserRegistrationDTO;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	
	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = new ModelMapper();
	}
	
	public Page<UserDTO> getAll(Pageable pageable) {
		Page<User> page = userRepository.findAll(pageable);
		return page.map(user -> modelMapper.map(user, UserDTO.class));
	}
	
	public Optional<UserDTO> getById(Long id) {
		Optional<User> optionalUser = userRepository.findById(id);
		return optionalUser.map(user -> modelMapper.map(user, UserDTO.class));
	}
	
	public Optional<UserDTO> getUserAccount(Long id) {
		Optional<User> optionalUser = userRepository.findById(id);
		return optionalUser.map(user -> modelMapper.map(user, UserDTO.class));
	}
	
	public Optional<UserProfileDTO> getUserPublicProfile(Long id) {
		Optional<User> optionalUser = userRepository.findById(id);
		return optionalUser.map(user -> modelMapper.map(user, UserProfileDTO.class));
	}
	
	/*public UserDTO create(UserDTO userDTO) {
		// TODO set role
		User user = modelMapper.map(userDTO, User.class);
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserDTO.class);
	}*/
	
	public void deleteById(Long id) {
		userRepository.deleteById(id);
	}
	
	public Optional<UserDTO> update(UserDTO userDTO) {
		Optional<User> optionalUser = userRepository.findById(userDTO.getId());
		if (optionalUser.isEmpty()) {
			return Optional.empty();
		} else {
			User user = optionalUser.get();
			user.setBanned(userDTO.isBanned());
			User saved = userRepository.save(user);
			return Optional.of(modelMapper.map(saved, UserDTO.class));
		}
	}
	
	public boolean existsUserByUsername(String username) {
		return userRepository.existsUserByUsername(username);
	}
	
	public Optional<UserDTO> getUserByUsername(String username) {
		return userRepository.findByUsername(username).map(user -> modelMapper.map(user, UserDTO.class));
	}
	
	public boolean existsUserByUsernameAndIdIsNot(String username, Long id) {
		return userRepository.existsUserByUsernameAndIdIsNot(username, id);
	}
	
	public boolean existsUserByEmail(String email) {
		return userRepository.existsUserByEmail(email);
	}
	
	public UserDTO register(UserRegistrationDTO userRegistrationDTO) {
		User user = modelMapper.map(userRegistrationDTO, User.class);
		user.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
		user.setActive(false);
		user.setBanned(false);
		roleRepository.findRoleByNameIgnoreCase(RoleContainer.USER).ifPresent(user::setRole);
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserDTO.class);
	}
	
	public boolean authenticate(String username, String password) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		User user = userOptional.orElse(null);
		return user != null && BCrypt.checkpw(password, user.getPassword());
	}
	
}
