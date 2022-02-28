package com.example.demo.threads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
	@EntityGraph(attributePaths = {"category", "user"})
	@Query(value = """
			SELECT t AS thread, lp AS lastPost, count(p) as postsCount FROM Thread t
			JOIN t.category tc
			JOIN t.user tu
			LEFT JOIN Post p ON p.thread.id=t.id
			LEFT JOIN FETCH Post lp ON t.id=lp.thread.id
			AND lp.createDate=(SELECT MAX(pp.createDate) FROM Post pp WHERE pp.thread.id=t.id)
			JOIN lp.user lpu
			JOIN lp.user.role lpur
			WHERE t.category.id=?1 AND t.pinned=TRUE GROUP BY t.id, tc, tu, lp.id ORDER BY lp.createDate DESC
			""")
	List<TestSimpleProj> findAllByCategoryIdAndPinnedIsTrue(Long id);
	
	@EntityGraph(attributePaths = {"category", "user"})
	@Query(value = """
			SELECT t AS thread, lp AS lastPost, count(p) as postsCount FROM Thread t
			JOIN t.category tc
			JOIN t.user tu
			LEFT JOIN Post p ON p.thread.id=t.id
			LEFT JOIN FETCH Post lp ON t.id=lp.thread.id
			AND lp.createDate=(SELECT MAX(pp.createDate) FROM Post pp WHERE pp.thread.id=t.id)
			JOIN lp.user lpu
			JOIN lp.user.role lpur
			WHERE t.category.id=?1 AND t.pinned=FALSE GROUP BY t.id, tc, tu, lp.id ORDER BY lp.createDate DESC
			""")
	Page<TestSimpleProj> findAllByCategoryIdAndPinnedIsFalse(Long id, Pageable pageable);
	
	@EntityGraph(attributePaths = {"category", "user", "user.role"})
	@Query("select t as thread from Thread t where t.id = ?1")
	Optional<TestPurposesProj> getThreadByIdTest(Long aLong);

	
	/*@Query("SELECT t.id, t.title, (count(*) FROM Post p) FROM Thread t "+
			"WHERE t.id=?1")
	TestSimpleProj getThreadWithoutCategoryById(Long id);*/
	
}