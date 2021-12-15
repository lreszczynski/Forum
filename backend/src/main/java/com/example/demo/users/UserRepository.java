package com.example.demo.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	
	boolean existsUserByUsername(String username);
	
	boolean existsUserByUsernameAndIdIsNot(String username, Long id);
	
	boolean existsUserByEmail(String email);
}