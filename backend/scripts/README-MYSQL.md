# MySQL 接入说明

## 1. 本机准备

- 安装 MySQL 8（或 5.7），并启动服务。
- 记下 root 密码（或你使用的用户名/密码）。

## 2. 创建数据库（首次）

在 MySQL 中执行：

```sql
CREATE DATABASE IF NOT EXISTS yitong_guides CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 3. 修改连接配置

编辑 `src/main/resources/application-mysql.yml`：

- `spring.datasource.url`：若端口不是 3306 或库名不同，请修改。
- `spring.datasource.username` / `password`：改成你的 MySQL 用户名和密码。

## 4. 使用 MySQL 启动后端

- 命令行：`./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`
- 或在 IDE 中设置 **Active profiles** 为 `mysql` 后运行。

首次启动后，JPA 会按实体自动建表（`ddl-auto: update`）。

## 5. 清空用户与帖子（保留表结构）

需要清空所有用户、攻略及关联数据时，执行：

```bash
mysql -u root -p yitong_guides < scripts/mysql-clear-tables.sql
```

或在 MySQL 客户端中执行 `scripts/mysql-clear-tables.sql` 中的 SQL。

清空后请重启应用，默认管理员会由 `AdminInitializer` 根据配置重新创建（默认 admin / admin123，见 application.yml 中 `app.bootstrap.admin`）。
