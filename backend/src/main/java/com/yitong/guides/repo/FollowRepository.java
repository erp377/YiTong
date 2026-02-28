package com.yitong.guides.repo;

import com.yitong.guides.domain.Follow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
  boolean existsByUserIdAndFollowUserId(Long userId, Long followUserId);
  void deleteByUserIdAndFollowUserId(Long userId, Long followUserId);

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.followUserId = :followUserId")
  void deleteByFollowUserId(@Param("followUserId") Long followUserId);

  List<Follow> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<Follow> findByFollowUserIdOrderByCreatedAtDesc(Long followUserId);
}
