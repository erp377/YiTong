package com.yitong.guides.repo;

import com.yitong.guides.domain.Guide;
import com.yitong.guides.domain.GuideCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuideRepository extends JpaRepository<Guide, Long> {
  @EntityGraph(attributePaths = {"author"})
  Page<Guide> findByCategoryAndDeletedFalse(GuideCategory category, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  @Query(
      """
      select g from Guide g
      where g.deleted = false
        and (:category is null or g.category = :category)
        and (:q is null or lower(g.title) like concat('%', lower(:q), '%')
          or lower(g.contentMarkdown) like concat('%', lower(:q), '%'))
      """)
  Page<Guide> search(
      @Param("category") GuideCategory category, @Param("q") String q, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  Page<Guide> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);

  /** 查询某用户全部攻略（含已逻辑删除），用于管理员删除用户时级联删除 */
  Page<Guide> findByAuthor_IdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  @Query("select g from Guide g where g.deleted = false and g.author.id in :authorIds order by g.createdAt desc")
  Page<Guide> findByAuthor_IdInAndDeletedFalseOrderByCreatedAtDesc(
      @Param("authorIds") java.util.List<Long> authorIds, Pageable pageable);

  @Query("select g from Guide g join fetch g.author where g.id = :id and g.deleted = false")
  Optional<Guide> findByIdWithAuthor(@Param("id") Long id);

  boolean existsByIdAndDeletedFalse(Long id);
}

