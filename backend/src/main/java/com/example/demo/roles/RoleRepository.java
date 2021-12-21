package com.example.demo.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	boolean existsRoleByName(String name);
	
	boolean existsRoleByNameAndIdIsNot(String name, Long id);
	
	Optional<Role> findByName(String name);
	
	/*@Query("SELECT role FROM Role role " +
			"LEFT JOIN CategoryRole categoryRole ON role.id=categoryRole.roleId " +
			"WHERE categoryRole.categoryId = ?1")
	List<Role> getRolesByCategoryId(Long id);
	
	@Query("SELECT role FROM Role role " +
			"LEFT JOIN CategoryRole categoryRole ON role.id=categoryRole.roleId " +
			"WHERE categoryRole.categoryId = ?1 AND categoryRole.roleId = ?2")
	Optional<Role> findRoleByCategoryIdAndRoleId(Long categoryId, Long roleId);*/
	
	Set<Role> getRolesByCategoriesId(Long categoryId);
	
	Optional<Role> findRoleByIdAndCategoriesId(Long roleId, Long categoryId);
	
	Optional<Role> findRoleByNameIgnoreCase(String name);
}