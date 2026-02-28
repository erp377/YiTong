package com.yitong.guides.web.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplatesController {
  public record TemplateResp(String key, String name, String categoryHint, String starterMarkdown) {}

  @GetMapping
  public List<TemplateResp> list() {
    return List.of(
        new TemplateResp(
            "itinerary_table",
            "行程表模板",
            "TRAVEL",
            """
            # 行程安排（示例）

            | 天数 | 城市/地点 | 交通 | 住宿 | 预算 | 备注 |
            |---|---|---|---|---:|---|
            | Day1 |  |  |  |  |  |
            | Day2 |  |  |  |  |  |
            """),
        new TemplateResp(
            "study_plan",
            "学习计划模板",
            "STUDY",
            """
            # 学习计划（示例）

            ## 目标
            - 

            ## 每周计划
            | 周次 | 主题 | 任务 | 预计用时 | 产出/验收 |
            |---|---|---|---:|---|
            | Week1 |  |  |  |  |
            | Week2 |  |  |  |  |

            ## 打卡建议
            - 每天记录：进度（0-100）+ 今日完成内容 + 明日计划
            """),
        new TemplateResp(
            "game_build",
            "游戏配装/打法模板",
            "GAME",
            """
            # 配装/Build（示例）

            ## 适用场景
            - 

            ## 核心思路
            - 

            ## 装备/技能/符文
            - 

            ## 操作要点
            - 
            """));
  }
}

