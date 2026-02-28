-- 易通攻略 - MySQL 清空用户表与帖子表（及所有关联表）
-- 使用前请确保已创建数据库：CREATE DATABASE IF NOT EXISTS yitong_guides CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 执行方式：mysql -u root -p yitong_guides < mysql-clear-tables.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE follows;
TRUNCATE TABLE study_checkins;
TRUNCATE TABLE guide_likes;
TRUNCATE TABLE guide_favorites;
TRUNCATE TABLE comments;
TRUNCATE TABLE guides;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '用户表与帖子表已清空，可自行添加测试数据。' AS message;
