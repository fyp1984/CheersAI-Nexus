# CheersAI-Nexus 部署套件

本目录包含 CheersAI-Nexus 项目的完整部署、运维脚本和配置文件。

## 目录结构

```
deployment/
├── scripts/                    # 部署脚本
│   ├── build.sh               # Linux/macOS 构建脚本
│   ├── build.ps1              # Windows PowerShell 构建脚本
│   ├── deploy.sh              # Linux/macOS 部署脚本
│   ├── deploy.ps1              # Windows PowerShell 部署脚本
│   ├── rollback.sh            # 回滚脚本 (Linux)
│   ├── service.sh             # 服务管理脚本 (Linux)
│   └── server-setup.sh        # 服务器初始化脚本 (Linux)
├── systemd/                    # systemd 服务文件
│   ├── nexus-auth.service
│   ├── nexus-user-management.service
│   ├── nexus-feedback.service
│   ├── nexus-product.service
│   ├── nexus-membership.service
│   └── nexus-auditlog.service
├── nginx/                      # Nginx 配置
│   ├── nexus.conf             # 主配置文件
│   └── ssl-setup.sh           # SSL 证书安装脚本
├── jenkins/                    # Jenkins 配置
│   ├── Jenkinsfile            # CI/CD Pipeline
│   └── jenkins-agent-setup.sh # Agent 安装脚本
├── config/                     # 应用配置模板
│   ├── auth/application-prod.yml
│   ├── user-management/application-prod.yml
│   ├── feedback/application-prod.yml
│   ├── product/application-prod.yml
│   ├── membership/application-prod.yml
│   ├── auditlog/application-prod.yml
│   └── .env.example           # 环境变量示例
└── docs/                       # 文档
    └── OPERATIONS_MANUAL.md   # 完整运维手册
```

## 快速开始

### 1. 服务器初始化

```bash
# 上传脚本到服务器
scp -r deployment root@175.178.236.183:/home/nexus/

# SSH 到服务器并执行初始化
ssh root@175.178.236.183
sudo bash /home/nexus/deployment/scripts/server-setup.sh --full
```

### 2. 上传 SSL 证书

```powershell
# Windows PowerShell
scp -r uat-nexus.cheersai.cloud_nginx root@175.178.236.183:/home/nexus/
```

```bash
# Linux/macOS Bash
scp -r uat-nexus.cheersai.cloud_nginx root@175.178.236.183:/home/nexus/
```

### 3. 配置 SSL 证书和 Nginx

```bash
ssh root@175.178.236.183
sudo bash /home/nexus/deployment/nginx/ssl-setup.sh --all
```

### 4. 本地构建并部署

**Windows (PowerShell):**

```powershell
cd deployment\scripts
.\build.ps1 -Environment uat
.\deploy.ps1
```

**Linux/macOS (Bash):**

```bash
cd deployment/scripts
./build.sh uat
./deploy.sh
```

## 详细文档

请参考 [OPERATIONS_MANUAL.md](./docs/OPERATIONS_MANUAL.md) 获取完整的运维手册。

## 服务列表

| 服务 | 端口 | API 路径 | 描述 |
|------|------|----------|------|
| auth | 8082 | /api/v1/auth | 认证服务 |
| user-management | 8083 | /api/v1/users | 用户管理 |
| feedback | 8084 | /api/v1/feedbacks | 反馈服务 |
| product | 8085 | /api/v1/products | 产品服务 |
| membership | 8086 | /api/v1/membership | 会员管理 |
| auditlog | 8087 | /api/v1/audit-logs | 审计日志 |

## 环境要求

### 构建机器

**Windows:**
- Windows 10/11 或 Windows Server
- Java 21 (推荐BellSoft JDK)
- Node.js 20+
- Maven 3.9+

**Linux/macOS:**
- Bash shell
- Java 21
- Node.js 20+
- Maven 3.9+

### 部署服务器

- Linux (CentOS/RHEL/Ubuntu)
- Java 21
- PostgreSQL 16
- Redis 7
- Nginx 1.20+
- SSL 证书