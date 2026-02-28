package com.yitong.guides.service;

import com.yitong.guides.domain.Follow;
import com.yitong.guides.repo.FollowRepository;
import com.yitong.guides.repo.UserRepository;
import com.yitong.guides.web.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  public FollowService(FollowRepository followRepository, UserRepository userRepository) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
  }

  /** 关注用户 */
  @Transactional
  public void follow(Long currentUserId, Long targetUserId) {
    if (targetUserId.equals(currentUserId)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "不能关注自己");
    }
    if (!userRepository.existsById(targetUserId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
    }
    if (followRepository.existsByUserIdAndFollowUserId(currentUserId, targetUserId)) {
      return;
    }
    Follow f = new Follow();
    f.setUserId(currentUserId);
    f.setFollowUserId(targetUserId);
    followRepository.save(f);
  }

  /** 取消关注 */
  @Transactional
  public void unfollow(Long currentUserId, Long targetUserId) {
    followRepository.deleteByUserIdAndFollowUserId(currentUserId, targetUserId);
  }

  public boolean existsByUserIdAndFollowUserId(Long userId, Long followUserId) {
    return followRepository.existsByUserIdAndFollowUserId(userId, followUserId);
  }
}
