package com.example.demo.categoryroles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRoleRepository extends JpaRepository<CategoryRole, CategoryRolePK> {
	Set<CategoryRole> getByCategoryId(Long categoryId);
	Optional<CategoryRole> findByCategoryIdAndRoleId(Long categoryId, Long roleId);
}