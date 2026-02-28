# 易通攻略：多场景攻略分享平台（Spring Boot + Vue）

题目：**多场景攻略分享平台的设计与实现**  
核心：聚合 **旅游 / 游戏 / 学习** 三类攻略，支持发布（模板）、分类/搜索浏览、点赞评论收藏互动；学习类支持打卡记录进度。  
架构：**前后端分离**（`frontend/` Vue3，`backend/` Spring Boot REST API）。

## 运行前准备

你当前机器环境：已检测到 **JDK 17**；未检测到 **Node / npm**，也未检测到系统级 **Maven/Gradle**。

- **后端**：本项目使用 **Maven Wrapper**（无需单独安装 Maven）。
- **前端**：需要安装 **Node.js LTS（建议 20.x 或 22.x）**，安装后打开新终端应能运行 `node -v` 与 `npm -v`。

## 启动后端（Spring Boot）

在项目根目录执行：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认使用 **H2** 文件数据库。若要用 **MySQL**，请先创建数据库并修改 `backend/src/main/resources/application-mysql.yml` 中的连接信息，然后以 profile `mysql` 启动（例如 `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`）。清空用户与帖子表见 `backend/scripts/README-MYSQL.md`。

默认：
- API：`http://localhost:8080`
- OpenAPI 文档：`http://localhost:8080/swagger-ui.html`
- H2 控制台：`http://localhost:8080/h2-console`（仅默认 H2 时）

## 启动前端（Vue 3）

安装好 Node.js 后，在项目根目录执行：

```powershell
cd frontend
npm install
npm run dev
```

前端默认：`http://localhost:5173`（已配置代理到后端 `:8080`）。

## 目录结构

- `backend/`：Spring Boot API（JWT 鉴权、攻略/评论/点赞/收藏/学习打卡）
- `frontend/`：Vue3 页面（分类浏览/搜索、详情、发布（模板）、互动、学习打卡）

