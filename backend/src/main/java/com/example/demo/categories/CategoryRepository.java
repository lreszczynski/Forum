package com.example.demo.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	boolean existsCategoryByName(String name);
	
	boolean existsCategoryByNameAndIdIsNot(String name, Long id);
}