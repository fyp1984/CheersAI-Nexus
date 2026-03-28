# CheersAI-Nexus

CheersAI项目群管理平台 - 基于微服务架构的前后端分离系统

## 项目简介

CheersAI-Nexus 是一套基于 Spring Boot + Vue 3 的CheersAI项目群微服务管理系统，提供用户管理、产品管理、会员订阅、用户反馈处理及审计日志等功能。

## 技术栈

### 后端
- **运行时**: Java 21
- **框架**: Spring Boot 3.5.x
- **ORM**: MyBatis-Flex
- **数据库**: PostgreSQL 16
- **缓存**: Redis 7
- **数据库迁移**: Flyway
- **认证**: JWT + Spring Security

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite 5
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4

## 项目结构

```
CheersAI-Nexus/
├── nexus-backend/          # 后端微服务项目
│   ├── common/              # 公共模块
│   ├── auth/                # 认证服务 (8082)
│   ├── user-management/     # 用户管理 (8083)
│   ├── feedback/            # 用户反馈 (8084)
│   ├── product/             # 产品管理 (8085)
│   ├── membership/          # 会员管理 (8086)
│   └── auditlog/            # 审计日志 (8087)
├── nexus-frontend/          # 前端 Vue 项目
└── deployment/             # 部署配置
```

## 服务列表

| 服务 | 端口 | API 路径 | 描述 |
|------|------|----------|------|
| auth | 8082 | /api/v1/auth | 认证服务 |
| user-management | 8083 | /api/v1/users | 用户管理 |
| feedback | 8084 | /api/v1/feedbacks | 用户反馈 |
| product | 8085 | /api/v1/products | 产品管理 |
| membership | 8086 | /api/v1/membership | 会员管理 |
| auditlog | 8087 | /api/v1/audit-logs | 审计日志 |

## 快速开始

### 前置要求

- JDK 21 (推荐BellSoft JDK)
- Node.js 20+
- Maven 3.9+
- PostgreSQL 16
- Redis 7

### 后端启动

```bash
cd nexus-backend

# 构建项目
mvn clean package -DskipTests

# 启动认证服务（首先启动）
java -jar auth/target/nexus-auth.jar

# 启动其他服务（并行）
java -jar user-management/target/nexus-user-management.jar &
java -jar feedback/target/nexus-feedback.jar &
java -jar product/target/nexus-product.jar &
java -jar membership/target/nexus-membership.jar &
java -jar auditlog/target/nexus-auditlog.jar &
```

### 前端启动

```bash
cd nexus-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 主要功能

- ✅ 用户管理（CRUD、批量操作、状态管理）
- ✅ 产品管理（产品、版本、操作日志）
- ✅ 会员管理（会员计划、订阅管理）
- ✅ 用户反馈（提交、处理、分配）
- ✅ 审计日志（操作追溯）
- ✅ 系统配置（参数管理、IP白名单）
- ✅ JWT 认证 + 短信验证码

## 相关文档

- [部署文档](./deployment/README.md)
- [运维手册](./deployment/docs/OPERATIONS_MANUAL.md)

## 开发团队

CheersAI Team
