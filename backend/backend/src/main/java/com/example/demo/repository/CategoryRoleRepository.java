package com.example.demo.repository;

import com.example.demo.model.CategoryRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRoleRepository extends JpaRepository<CategoryRole, Long> {
}