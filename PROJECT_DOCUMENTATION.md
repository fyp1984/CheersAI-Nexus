# CheersAI-Nexus 项目技术文档

## 一、项目概述

CheersAI-Nexus 是一套基于微服务架构的全栈管理系统，采用 Spring Boot 构建后端服务集群，配合 Vue 3 构建前端管理界面。项目采用前后端分离的架构模式，后端通过 Spring Cloud OpenFeign 实现服务间通信，前端通过 Vite 代理请求转发至各后端服务。该系统主要用于企业级用户管理、产品管理、会员订阅、用户反馈处理及审计日志记录等核心业务场景。

从技术选型角度来看，后端选用了 Java 21 作为运行时环境，Spring Boot 3.5.x 作为应用框架，MyBatis-Flex 作为持久层框架，PostgreSQL 作为主数据库，Redis 作为缓存与会话存储，Flyway 管理数据库版本迁移。前端则选用 Vue 3 搭配 TypeScript，使用 Vite 作为构建工具，Element Plus 作为 UI 组件库，Pinia 进行状态管理，Vue Router 处理路由导航。这套技术栈确保了系统具备良好的性能表现与可维护性，同时满足企业级应用的扩展性要求。

## 二、项目目录结构

项目采用前后端分离的目录组织方式，整体结构如下：

```
CheersAI-Nexus/
├── nexus-backend/          # 后端微服务项目
│   ├── pom.xml            # 父 POM 文件
│   ├── common/            # 公共模块
│   ├── auth/              # 认证服务
│   ├── user-management/   # 用户管理服务
│   ├── feedback/          # 用户反馈服务
│   ├── product/          # 产品管理服务
│   ├── membership/        # 会员订阅服务
│   └── auditlog/          # 审计日志服务
├── nexus-frontend/        # 前端 Vue 项目
│   ├── src/
│   │   ├── api/          # API 接口定义
│   │   ├── components/   # 公共组件
│   │   ├── views/        # 页面视图
│   │   ├── router/       # 路由配置
│   │   ├── store/        # Pinia 状态管理
│   │   ├── utils/        # 工具函数
│   │   ├── types/        # TypeScript 类型定义
│   │   ├── styles/       # 样式文件
│   │   └── layout/       # 布局组件
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
└── README.md
```

## 三、后端架构详解

### 3.1 技术栈总览

后端基于 Spring Boot 3.5.11 构建，采用 Maven 多模块结构管理各微服务。主要依赖版本如下：Java 21 作为编译与运行环境，MyBatis-Flex 1.11.6 作为 ORM 框架，PostgreSQL 42.7.3 作为数据库驱动，JWT（jjwt）0.12.6 用于令牌签发与验证，Flyway 10.17.0 管理数据库版本迁移，Spring Cloud 2025.0.1 提供微服务能力，Redisson 3.52.0 操作 Redis。此外，项目还集成了 Hutool 5.8.28 工具包、BouncyCastle 国密算法库以及阿里云短信 SDK。

### 3.2 模块职责划分

后端包含七个独立部署的微服务模块，每个模块负责特定的业务领域，通过 HTTP 接口对外提供服务。各模块的端口配置与核心职责如下所述。

**公共模块（common）** 是整个后端的基础依赖库，不包含启动类，仅提供各服务共用的工具类与基础组件。该模块包含国密 SM2、SM4 加密算法实现（位于 com.cheersai.nexus.common.security 包下），JSON 序列化工具 JacksonUtils，统一响应结果封装类 Result 与 ResultDeprecated，基础实体类 Input，以及 MyBatis-Flex 配置类。其他所有业务模块都将 common 作为依赖引入，以确保代码复用与规范统一。

**认证服务（auth）** 监听端口 8082，是系统的统一认证入口，负责用户注册、登录、令牌刷新、验证码发送等核心功能。该服务集成了 Redis 用于会话存储与验证码缓存，集成阿里云短信服务发送手机验证码，使用 JWT 实现无状态身份认证。AuthApplication 作为启动类，核心控制器为 AuthController，提供 /api/v1/auth/* 接口路径。安全配置方面，该模块整合了 Spring Security 与自定义 JwtAuthenticationFilter，实现完整的认证授权流程。数据库设计包含 verification_codes 表存储验证码、refresh_tokens 表管理刷新令牌、audit_logs 表记录认证日志。

**用户管理服务（user-management）** 监听端口 8083，负责系统用户的增删改查、批量状态更新、密码重置等用户全生命周期管理功能。UserManagementApplication 作为启动类，UserManagementController 提供 /api/v1/users/* 接口路径。核心 DTO 包括用户创建请求 UserCreateDTO、用户更新请求 UserUpdateDTO、用户列表查询 UserListQueryDTO、用户状态批量更新 UserStatusBatchUpdateDTO 等。服务层通过 UserService 与 UserServiceImpl 实现业务逻辑，数据库持久化使用 MyBatis-Flex 框架。

**用户反馈服务（feedback）** 监听端口 8084，专注于用户反馈意见的收集、处理与跟踪管理。FeedbackApplication 作为启动类，FeedbackController 提供 /api/v1/feedbacks/* 接口路径。该模块使用 Flyway 进行数据库迁移，V1 版本迁移脚本创建 feedbacks 表存储反馈记录。数据类型支持 JSONB 格式存储，便于扩展反馈附件与处理历史。

**产品管理服务（product）** 监听端口 8085，负责产品信息的维护、产品版本管理以及产品操作日志记录。ProductApplication 作为启动类，ProductController 提供 /api/v1/products/* 接口路径。核心实体包括 Product（产品主表）、ProductVersion（产品版本）、ProductOperationLog（操作日志）。数据库迁移包含 V1 版本创建产品相关表、V2 版本创建操作日志表。该模块支持产品的批量删除、功能特性管理等功能。

**会员订阅服务（membership）** 负责会员计划管理与订阅服务，目前已完成基础框架搭建。MembershipApplication 作为启动类，核心服务包括 PlanService 管理会员计划、SubscriptionService 处理订阅业务。该模块同样依赖 common 模块，使用 MyBatis-Flex 进行数据持久化。

**审计日志服务（auditlog）** 负责记录并查询系统操作审计日志，提供完整的操作追溯能力。AuditLogApplication 作为启动类，AuditLogController 提供 /api/v1/audit-logs/* 接口路径。该模块集成 Redis 进行日志缓存以提升查询性能，使用 Spring Cloud OpenFeign 调用其他服务获取操作记录。核心实体 AuditLog 记录操作人、操作时间、操作类型、操作内容等关键信息。

### 3.3 API 路由对照表

后端各服务提供的 API 接口路径与端口映射关系如下：

| 服务名称 | 端口 | API 前缀 | 主要功能 |
|---------|------|----------|----------|
| auth | 8082 | /api/v1/auth | 登录、注册、验证码、令牌刷新、当前用户 |
| user-management | 8083 | /api/v1/users | 用户列表、创建、编辑、删除、状态管理 |
| feedback | 8084 | /api/v1/feedbacks | 反馈列表、创建、处理、分配 |
| product | 8085 | /api/v1/products | 产品列表、版本管理、操作日志 |
| membership | 8086 | /api/v1/membership | 会员计划、订阅管理 |
| auditlog | 8087 | /api/v1/audit-logs | 审计日志查询 |

### 3.4 数据库设计

项目使用 PostgreSQL 作为主数据库，采用 Flyway 进行数据库版本管理。各模块独立的数据库迁移脚本位于 src/main/resources/db/migration 目录下。当前已存在的迁移脚本包括：feedback 模块的 V1__create_feedbacks_table.sql，product 模块的 V1__create_product_tables.sql 与 V2__create_product_operation_logs.sql。

数据库连接配置通过各模块的 application.yaml 文件管理，核心配置项包括数据库 URL、用户名、密码、连接池参数等。数据源使用 Druid 连接池进行管理，MyBatis-Flex 作为 ORM 框架处理对象关系映射。

**数据库迁移脚本列表：**

| 模块 | 迁移文件 | 说明 |
|------|----------|------|
| auth | (外部管理) | users 表由 user-management 模块创建 |
| user-management | V3__alter_users_add_management_fields.sql | 用户扩展字段 |
| system-config | V1__create_system_configs_table.sql, V2__create_ip_whitelist_table.sql | 系统配置表、IP白名单表 |
| product | V1__create_product_tables.sql, V2__create_product_operation_logs.sql | 产品表、产品版本表 |
| feedback | V1__create_feedbacks_table.sql | 反馈表 |
| membership | V1__create_membership_tables.sql, V2__create_audit_log_tables.sql | 会员计划表、订阅表、审计日志表 |
| auditlog | V1__create_audit_logs_table.sql | 审计日志表 |

## 四、前端架构详解

### 4.1 技术栈配置

前端项目基于 Vue 3.4.38 构建，使用 TypeScript 5.4.5 进行类型检查，Vite 5.4.1 作为开发服务器与构建工具。UI 组件库选用 Element Plus 2.7.8，提供企业级美观的组件风格。状态管理采用 Pinia 2.1.7，路由管理使用 Vue Router 4.4.5。HTTP 请求库使用 Axios 1.6.8，通过封装后的 request 工具统一处理请求拦截、响应拦截与错误处理。

### 4.2 项目结构

前端源代码组织结构清晰，采用功能模块化划分方式。主要目录包括：api 目录存放各业务模块的 API 接口定义，views 目录包含各页面视图组件，router 目录配置路由规则与导航守卫，store 目录使用 Pinia 管理全局状态，utils 目录提供工具函数，types 目录定义 TypeScript 类型，styles 目录存放全局样式，components 目录包含公共组件，layout 目录定义应用布局结构。

### 4.3 路由配置

前端路由配置定义在 src/router/index.ts 文件中，采用 Vue Router 的动态导入方式实现路由懒加载。主要路由如下：

| 路由路径 | 页面名称 | 功能说明 | 优先级 |
|----------|----------|----------|--------|
| /login | LoginView | 用户登录 | 公共 |
| /register | RegisterView | 用户注册 | 公共 |
| /dashboard | DashboardView | 数据分析看板 | P2 |
| /product/list | ProductListView | 产品管理列表 | P0 |
| /pricing/config | PricingConfigView | 功能定价配置 | P1 |
| /user/list | UserListView | 用户管理列表 | P0 |
| /member/plans | MemberPlansView | 会员计划管理 | P0 |
| /feedback/list | FeedbackListView | 用户反馈列表 | P0 |
| /audit/logs | AuditLogsView | 审计日志查询 | P1 |
| /notice/list | NoticeListView | 公告列表管理 | P1 |
| /system/config | SystemConfigView | 系统参数配置 | P0 |

路由导航守卫实现了简单的权限控制：未登录用户自动重定向至登录页，已登录用户访问登录或注册页自动跳转至仪表盘。

### 4.4 API 接口封装

前端通过封装 request 工具（位于 src/utils/request.ts）统一处理 HTTP 请求。该工具基于 Axios 实例创建，配置了请求超时时间、Content-Type 类型，并实现了请求拦截器与响应拦截器。请求拦截器自动携带 JWT 令牌，响应拦截器统一处理错误响应与令牌过期场景。

各业务模块的 API 接口分别定义在 src/api 目录下的独立文件中：auth.ts 处理认证相关接口，users.ts 处理用户管理接口，products.ts 处理产品管理接口，feedback.ts 处理用户反馈接口，membership.ts 处理会员接口，audit.ts 处理审计日志接口。接口调用统一使用 unwrapApiData 工具函数解包响应数据。

### 4.5 状态管理

前端使用 Pinia 进行全局状态管理，主要模块包括认证状态（src/store/modules/auth.ts）。认证状态存储当前登录用户信息、访问令牌与刷新令牌，并提供登录、登出、令牌刷新等状态变更方法。应用入口（src/main.ts）初始化 Pinia 并挂载到 Vue 应用实例。

### 4.6 开发环境配置

前端开发环境通过 Vite 配置代理实现请求转发。vite.config.ts 文件中的 server.proxy 配置将 /api/v1/auth 路径转发至 http://localhost:8082，/api/v1/users 转发至 http://localhost:8083，/api/v1/feedbacks 转发至 http://localhost:8084，/api/v1/products 转发至 http://localhost:8085。这种代理配置解决了开发阶段的跨域问题，同时模拟了生产环境的网关路由结构。

环境变量配置文件 .env.example 定义了必要的配置项：VITE_API_BASE_URL 设置 API 基础路径，VITE_AES_KEY 与 VITE_HMAC_KEY 用于数据加密，VITE_DEV_LOGIN_IDENTITY 与 VITE_DEV_LOGIN_PASSWORD 用于开发环境快速登录。

## 五、部署指南

### 5.1 环境要求

部署前需确保目标服务器满足以下软件环境要求。后端服务需要 Java 21 运行时环境，建议配置不低于 2 核 CPU 与 4GB 内存以保证性能。数据库需要 PostgreSQL 14.x 或更高版本，同时需要创建对应的数据库用户与数据库实例。缓存与会话存储需要 Redis 6.x 或更高版本。国密算法支持需要确保 OpenSSL 已安装以便 BouncyCastle 正常运作。前端部署需要 Nginx 1.20.x 或更高版本作为静态资源服务器。

### 5.2 后端部署步骤

后端部署按照模块依赖顺序依次启动，首先需要构建公共模块 common，然后依次启动各业务服务。具体步骤如下：

第一步，构建项目依赖。在 nexus-backend 目录下执行 Maven 命令构建整个项目，这将会编译所有模块并生成可执行的 JAR 包。构建命令为 mvn clean package -DskipTests，该命令会跳过测试以加快构建速度。构建完成后，各模块的 target 目录下将生成对应的 JAR 文件。

第二步，配置数据库连接。修改各模块 src/main/resources/application.yaml 文件中的数据库连接信息，包括数据库地址、端口、数据库名、用户名与密码。同样的方式配置 Redis 连接信息，包括 Redis 地址、端口、密码等。

第三步，初始化数据库。首次部署时需要运行 Flyway 迁移脚本创建数据库表结构。各模块启动时会自动检测并执行未应用的迁移脚本，确保数据库 schema 与代码版本一致。

第四步，启动服务。按照依赖顺序依次启动各微服务，建议的启动顺序为：auth（8082端口）→ user-management（8083端口）→ feedback（8084端口）→ product（8085端口）→ membership → auditlog。各服务启动命令均为 java -jar module-name.jar，服务启动后会监听各自配置的端口。

### 5.3 前端部署步骤

前端项目需要先进行生产环境构建，然后将构建产物部署至 Nginx 服务器。

第一步，安装依赖并构建。在 nexus-frontend 目录下执行 npm install 安装项目依赖，然后执行 npm run build 进行生产构建。构建完成后会在 dist 目录下生成静态资源文件。

第二步，配置 Nginx。创建 Nginx 配置文件，将 root 路径指向前端构建产物目录 dist，并配置合适的缓存策略与 gzip 压缩。同时需要配置代理规则将 API 请求转发至后端服务，或者配置负载均衡器进行路由。

第三步，启动 Nginx 服务。执行 nginx -t 验证配置文件的正确性，然后执行 nginx -s reload 或 nginx -s reopen 启动服务。服务启动后通过浏览器访问配置的域名或 IP 地址即可使用系统。

### 5.4 开发环境启动

本地开发时，后端各服务需要分别启动，前端通过 Vite 开发服务器运行。

后端启动方式：在各模块目录下执行 mvn spring-boot:run 命令启动对应的 Spring Boot 应用，或者使用 IDE 的运行配置直接启动带 @SpringBootApplication 注解的启动类。建议启动顺序与生产部署一致。

前端启动方式：在 nexus-frontend 目录下执行 npm install 安装依赖，然后执行 npm run dev 启动开发服务器。开发服务器默认监听 http://localhost:5173，通过浏览器访问即可查看应用。Vite 的热模块替换（HMR）功能支持代码修改后即时更新界面。

### 5.5 关键配置项

部署时需要特别关注以下配置项的正确设置。JWT 配置包括密钥、过期时间等，需确保前后端配置一致。数据库连接池参数需要根据实际负载情况进行调优。Redis 连接配置需要确保网络可达性。国密算法配置需要确保密钥的安全性。阿里云短信配置需要正确设置 AccessKey、SecretKey 与签名模板。跨域配置需要根据实际前端部署域名进行调整。

## 六、接口文档

### 6.1 认证服务接口

认证服务提供用户身份验证与令牌管理功能，是整个系统的安全入口。

**发送验证码** 接口路径为 POST /api/v1/auth/send-code，用于向用户手机号或邮箱发送验证码。请求体包含 purpose 字段标识验证码用途（register、login、reset_password、bind），以及 phone 或 email 字段标识接收端。成功响应返回空数据，验证码有效期由服务器配置决定。

**用户注册** 接口路径为 POST /api/v1/auth/register，用于创建新用户账户。请求体包含 username（用户名）、password（密码）、code（验证码），以及 phone 或 email（注册渠道）。成功响应返回 AuthResponse，包含 accessToken、refreshToken 与用户信息。

**用户登录** 接口路径为 POST /api/v1/auth/login，用于已有用户身份认证。请求体包含 password（密码），以及 phone 或 email（登录凭证）。成功响应返回 AuthResponse，包含访问令牌、刷新令牌与用户信息。

**令牌刷新** 接口路径为 POST /api/v1/auth/refresh，用于在访问令牌过期前刷新令牌。请求体包含 refreshToken。成功响应返回新的 AuthResponse。

**获取当前用户** 接口路径为 GET /api/v1/auth/me，用于获取当前登录用户的详细信息。需要携带有效的访问令牌。成功响应返回 UserInfo 对象。

**用户登出** 接口路径为 POST /api/v1/auth/logout，用于清除当前用户的会话状态。需要携带有效的访问令牌。成功响应返回空数据。

### 6.2 用户管理接口

用户管理服务提供系统用户的完整生命周期管理功能。

**获取用户列表** 接口路径为 GET /api/v1/users，支持分页查询与条件筛选。查询参数包括 page（页码）、pageSize（每页数量）、keyword（关键词）、status（用户状态）等。成功响应返回用户列表与分页信息。

**创建用户** 接口路径为 POST /api/v1/users，用于添加新用户。请求体包含用户名、密码、邮箱、手机号等用户信息。成功响应返回创建后的用户信息。

**更新用户** 接口路径为 PUT /api/v1/users/{id}，用于修改指定用户的信息。请求体包含需要更新的字段。成功响应返回更新后的用户信息。

**删除用户** 接口路径为 DELETE /api/v1/users/{id}，用于删除指定用户。成功响应返回空数据。

**批量更新状态** 接口路径为 PUT /api/v1/users/batch-status，用于批量修改用户状态。请求体包含用户 ID 列表与目标状态。成功响应返回操作结果。

### 6.3 产品管理接口

产品管理服务提供产品信息与版本的管理功能。

**获取产品列表** 接口路径为 GET /api/v1/products，支持分页查询与条件筛选。查询参数包括 page、pageSize、keyword 等。成功响应返回产品列表与分页信息。

**获取产品详情** 接口路径为 GET /api/v1/products/{id}，用于获取指定产品的详细信息，包括版本列表。

**创建产品** 接口路径为 POST /api/v1/products，用于添加新产品。请求体包含产品名称、描述、特性等信息。成功响应返回创建后的产品信息。

**更新产品** 接口路径为 PUT /api/v1/products/{id}，用于修改产品信息。

**删除产品** 接口路径为 DELETE /api/v1/products/{id}，支持单个或批量删除。

**获取产品操作日志** 接口路径为 GET /api/v1/products/{id}/operation-logs，用于查询产品的变更历史。

### 6.4 用户反馈接口

用户反馈服务提供反馈意见的收集与处理功能。

**获取反馈列表** 接口路径为 GET /api/v1/feedbacks，支持分页查询与状态筛选。

**创建反馈** 接口路径为 POST /api/v1/feedbacks，用于提交新的用户反馈。

**处理反馈** 接口路径为 PUT /api/v1/feedbacks/{id}/process，用于更新反馈处理状态与处理意见。

**分配反馈** 接口路径为 PUT /api/v1/feedbacks/{id}/assign，用于将反馈分配给指定处理人。

### 6.5 审计日志接口

审计日志服务提供系统操作的查询与追溯功能。

**获取审计日志列表** 接口路径为 GET /api/v1/audit-logs，支持按时间范围、操作人、操作类型等条件筛选。

## 七、安全机制

系统采用多层次的安全防护机制确保业务与数据安全。认证层面使用 JWT 无状态令牌，结合 Redis 存储刷新令牌实现双令牌机制。密码存储使用 BCrypt 强哈希算法加密，即使数据库泄露也无法直接获取明文密码。国密算法支持使用 SM2、SM4 对敏感数据进行加密存储与传输。API 访问控制通过 Spring Security 框架实现，基于角色与权限进行细粒度访问控制。审计日志记录所有敏感操作，便于安全审计与问题追溯。

## 八、扩展与优化建议

系统架构设计考虑了未来的扩展需求。在性能优化方面，可以引入 Redis 缓存减少数据库访问压力，使用连接池优化数据库连接管理，实现请求合并减少网络调用。在高可用方面，可以使用 Nginx 进行负载均衡，部署多实例服务实现故障转移，使用数据库主从复制提高数据可靠性。在功能扩展方面，可以方便地新增微服务模块，只需在父 POM 中添加模块依赖，在前端配置对应的代理与 API 即可。

---

本文档基于当前代码库分析生成，详细描述了 CheersAI-Nexus 项目的技术架构与部署流程。如有疑问或需要更新，请联系项目维护团队。
