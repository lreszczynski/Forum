package com.example.demo.categories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	boolean existsCategoryByName(String name);
	
	boolean existsCategoryByNameAndIdIsNot(String name, Long id);
	
	@Override
	@EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "roles")
	List<Category> findAll();
	
	@Override
	@EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "roles")
	Optional<Category> findById(Long aLong);
	
	/*@Query("SELECT r FROM Role r " +
			"WHERE r.id IN (SELECT cr.roleId FROM CategoryRole cr " +
			"WHERE cr.categoryId = 2 AND cr.roleId=1) ")
	Optional<Role> getByIdAndRoles_Id(Long categoryId, Long roleId);*/
}