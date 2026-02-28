package com.yitong.guides.bootstrap;

import com.yitong.guides.domain.Guide;
import com.yitong.guides.domain.GuideCategory;
import com.yitong.guides.domain.User;
import com.yitong.guides.domain.UserRole;
import com.yitong.guides.repo.GuideRepository;
import com.yitong.guides.repo.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {
  private final UserRepository userRepository;
  private final GuideRepository guideRepository;
  private final PasswordEncoder passwordEncoder;

  public DemoDataInitializer(
      UserRepository userRepository, GuideRepository guideRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.guideRepository = guideRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    // 仅在空库/近似空库时补充示例数据（避免污染你真实数据）
    if (userRepository.count() > 3 || guideRepository.count() > 0) {
      return;
    }

    User u1 = ensureUser("alice", "爱丽丝", "123456");
    User u2 = ensureUser("bob", "小波", "123456");
    User u3 = ensureUser("cathy", "小C", "123456");

    if (guideRepository.count() == 0) {
      guideRepository.save(
          buildGuide(
              u1,
              "成都3天2夜：吃喝玩乐不踩雷",
              GuideCategory.TRAVEL,
              "itinerary_table",
              """
              # 成都3天2夜行程（示例）

              关键词：宽窄巷子 / 春熙路 / 锦里 / 火锅 / 盖碗茶

              | 天数 | 城市/地点 | 交通 | 住宿 | 预算 | 备注 |
              |---|---|---|---|---:|---|
              | Day1 | 春熙路-太古里 | 地铁 | 春熙路附近 | 400 | 晚上火锅 |
              | Day2 | 宽窄巷子-人民公园 | 地铁+步行 | 同上 | 350 | 喝盖碗茶 |
              | Day3 | 锦里-武侯祠 | 打车 | - | 250 | 买伴手礼 |
              """));

      guideRepository.save(
          buildGuide(
              u2,
              "《某游戏》新手开荒：一套思路通关前期",
              GuideCategory.GAME,
              "game_build",
              """
              # 新手开荒思路（示例）

              ## 适用场景
              - 前期资源紧张
              - 想要稳定推进主线

              ## 核心思路
              - 先堆生存，再补输出
              - 优先拿到“通用型”装备/技能

              ## 装备/技能
              - 主武器：XXX
              - 防具：YYY
              - 技能：AAA + BBB

              ## 操作要点
              - BOSS 前先清小怪，保持药水与能量
              """));

      guideRepository.save(
          buildGuide(
              u3,
              "4周搞定 Vue3 入门学习计划（可打卡）",
              GuideCategory.STUDY,
              "study_plan",
              """
              # Vue3 入门学习计划（示例）

              ## 目标
              - 4周完成：组件化思维 + Router + Pinia + 基础工程化

              ## 每周计划
              | 周次 | 主题 | 任务 | 预计用时 | 产出/验收 |
              |---|---|---|---:|---|
              | Week1 | 基础语法 | ref/reactive、模板语法、生命周期 | 6h | 完成小练习 |
              | Week2 | 组件化 | props/emit、slot、组合式函数 | 8h | 组件库雏形 |
              | Week3 | 状态与路由 | Router、Pinia、权限思路 | 8h | 多页面Demo |
              | Week4 | 项目实战 | 做一个小项目并部署 | 10h | 完整可访问 |

              ## 打卡建议
              - 每天记录：进度（0-100）+ 今日完成内容 + 明日计划
              """));
    }
  }

  private User ensureUser(String username, String displayName, String rawPassword) {
    return userRepository
        .findByUsername(username)
        .orElseGet(
            () -> {
              User u = new User();
              u.setUsername(username);
              u.setDisplayName(displayName);
              u.setRole(UserRole.USER);
              u.setEnabled(true);
              u.setPasswordHash(passwordEncoder.encode(rawPassword));
              return userRepository.save(u);
            });
  }

  private Guide buildGuide(
      User author,
      String title,
      GuideCategory category,
      String templateKey,
      String contentMarkdown) {
    Guide g = new Guide();
    g.setAuthor(author);
    g.setTitle(title);
    g.setCategory(category);
    g.setTemplateKey(templateKey);
    g.setContentMarkdown(contentMarkdown);
    return g;
  }
}

