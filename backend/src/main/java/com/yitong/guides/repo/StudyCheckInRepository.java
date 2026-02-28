package com.yitong.guides.repo;

import com.yitong.guides.domain.StudyCheckIn;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyCheckInRepository extends JpaRepository<StudyCheckIn, Long> {
  List<StudyCheckIn> findByUserIdAndGuideIdOrderByDayAsc(Long userId, Long guideId);
  org.springframework.data.domain.Page<StudyCheckIn> findByUserIdOrderByCreatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);
  Optional<StudyCheckIn> findByUserIdAndGuideIdAndDay(Long userId, Long guideId, LocalDate day);
  /** 某攻略下去重后的打卡人数 */
  @Query("select count(distinct c.userId) from StudyCheckIn c where c.guideId = :guideId")
  long countDistinctUserIdByGuideId(@Param("guideId") Long guideId);
  void deleteByGuideId(Long guideId);
  void deleteByUserId(Long userId);
}

