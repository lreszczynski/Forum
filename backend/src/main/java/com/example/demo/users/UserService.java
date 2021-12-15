package com.example.demo.users;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = new ModelMapper();
	}
	
	public List<UserDTO> getAll() {
		Type listType = new TypeToken<List<UserDTO>>() {
		}.getType();
		return modelMapper.map(userRepository.findAll(), listType);
	}
	
	public Optional<UserDTO> getById(Long id) {
		Optional<User> optionalUser = userRepository.findById(id);
		return optionalUser.map(user -> modelMapper.map(user, UserDTO.class));
	}
	
	public UserDTO create(UserDTO userDTO) {
		// TODO set role
		User user = modelMapper.map(userDTO, User.class);
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserDTO.class);
	}
	
	public void deleteById(Long id) {
		userRepository.deleteById(id);
	}
	
	public Optional<UserDTO> update(Long id, UserDTO userDTO) {
		// TODO get entity then update
		if (!userDTO.getId().equals(id)) {
			return Optional.empty();
		} else {
			Optional<User> optionalUser = userRepository.findById(id);
			if (optionalUser.isEmpty()) {
				return Optional.empty();
			} else {
				User user = optionalUser.get();
				user.setUsername(userDTO.getUsername());
				User saved = userRepository.save(user);
				
				return Optional.of(modelMapper.map(saved, UserDTO.class));
			}
		}
	}
	
	public boolean existsUserByUsername(String username) {
		return userRepository.existsUserByUsername(username);
	}
	
	public Optional<User> getUserByUsername(String username) {
		return userRepository.findByUsername(username);
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
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserDTO.class);
	}
	
	public boolean authenticate(String username, String password) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		User user = userOptional.orElse(null);
		return user != null && BCrypt.checkpw(password, user.getPassword());
	}
	
}
