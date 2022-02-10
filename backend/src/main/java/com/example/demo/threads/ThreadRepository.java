package com.example.demo.threads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
	/*@Query(value = "SELECT t.id, t.title, (SELECT count(*) FROM App_Post p WHERE p.thread_id=?1), p FROM App_Thread t " +
			"LEFT JOIN App_Post p ON t.id=p.thread_id AND " +
			"p.create_date=(SELECT MAX(p.create_date) FROM App_Post p WHERE p.thread_id=t.id) " +
			"WHERE t.id=?1", nativeQuery = true)
	TestSimpleProj getThreadWithoutCategoryById(Long id);*/
	
	@Query(value = "SELECT t AS thread, ap AS lastPost FROM Thread t "+
			"LEFT JOIN Post ap ON t.id = ap.thread.id "+
			"AND ap.createDate=(SELECT MAX(p.createDate) FROM Post p WHERE p.thread.id=t.id) "+
			"WHERE t.category.id=?1 AND t.pinned=TRUE ORDER BY ap.createDate DESC")
	List<TestSimpleProj> findAllByCategoryIdAndPinnedIsTrue(Long id);
	
	@Query(value = "SELECT t AS thread, ap AS lastPost FROM Thread t "+
			"LEFT JOIN Post ap ON t.id = ap.thread.id "+
			"AND ap.createDate=(SELECT MAX(p.createDate) FROM Post p WHERE p.thread.id=t.id) "+
			"WHERE t.category.id=?1 AND t.pinned=FALSE ORDER BY ap.createDate DESC")
	Page<TestSimpleProj> findAllByCategoryIdAndPinnedIsFalse(Long id, Pageable pageable);
	
	/*@Query("SELECT t.id, t.title, (count(*) FROM Post p) FROM Thread t "+
			"WHERE t.id=?1")
	TestSimpleProj getThreadWithoutCategoryById(Long id);*/
	
}