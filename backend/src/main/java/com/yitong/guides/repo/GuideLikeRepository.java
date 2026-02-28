package com.yitong.guides.repo;

import com.yitong.guides.domain.GuideLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideLikeRepository extends JpaRepository<GuideLike, Long> {
  long countByGuideId(Long guideId);
  boolean existsByUserIdAndGuideId(Long userId, Long guideId);
  void deleteByUserIdAndGuideId(Long userId, Long guideId);
  void deleteByGuideId(Long guideId);
  void deleteByUserId(Long userId);
}

