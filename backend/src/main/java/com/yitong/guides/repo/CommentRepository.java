package com.yitong.guides.repo;

import com.yitong.guides.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("SELECT c FROM Comment c WHERE c.guide.id = :guideId ORDER BY c.createdAt ASC")
  List<Comment> findByGuideIdOrderByCreatedAtAsc(@Param("guideId") Long guideId);
  void deleteByGuide_Id(Long guideId);
  void deleteByUser_Id(Long userId);
}

