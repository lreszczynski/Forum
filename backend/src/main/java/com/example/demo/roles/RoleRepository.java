package com.example.demo.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	boolean existsRoleByName(String name);
	
	boolean existsRoleByNameAndIdIsNot(String name, Long id);
	
	@Query("SELECT role FROM Role role " +
			"WHERE role.id " +
			"IN (SELECT categoryRole.roleId FROM CategoryRole categoryRole " +
			"WHERE categoryRole.categoryId = ?1)")
	List<Role> getRolesByCategoryId(Long id);
	
	Optional<Role> findRoleByNameIgnoreCase(String name);
}