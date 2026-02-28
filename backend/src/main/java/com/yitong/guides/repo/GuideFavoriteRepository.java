package com.yitong.guides.repo;

import com.yitong.guides.domain.GuideFavorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideFavoriteRepository extends JpaRepository<GuideFavorite, Long> {
  long countByGuideId(Long guideId);
  boolean existsByUserIdAndGuideId(Long userId, Long guideId);
  void deleteByUserIdAndGuideId(Long userId, Long guideId);
  List<GuideFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
  void deleteByGuideId(Long guideId);
  void deleteByUserId(Long userId);
}

