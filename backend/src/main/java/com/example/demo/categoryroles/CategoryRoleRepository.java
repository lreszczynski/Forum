package com.example.demo.categoryroles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRoleRepository extends JpaRepository<CategoryRole, CategoryRolePK> {
}