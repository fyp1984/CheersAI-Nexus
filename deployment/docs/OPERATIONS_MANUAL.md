# CheersAI-Nexus 运维手册

## 目录

1. [系统架构](#1-系统架构)
2. [服务器配置](#2-服务器配置)
3. [部署流程](#3-部署流程)
4. [运维操作](#4-运维操作)
5. [备份与恢复](#5-备份与恢复)
6. [故障排查](#6-故障排查)
7. [监控与告警](#7-监控与告警)
8. [安全配置](#8-安全配置)
9. [附录](#9-附录)

---

## 1. 系统架构

### 1.1 组件架构

```
                              ┌─────────────────────────────────────┐
                              │          Nginx (SSL Termination)     │
                              │         uat-nexus.cheersai.cloud     │
                              └──────────────────┬──────────────────┘
                                                 │
                        ┌────────────────────────┼────────────────────────┐
                        │                        │                        │
                        ▼                        ▼                        ▼
              ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
              │   Frontend (Vue) │    │   Auth Service   │    │  User Management │
              │   Port: 80/443   │    │    Port: 8082    │    │    Port: 8083    │
              │   /dist          │    │   /api/v1/auth   │    │  /api/v1/users   │
              └──────────────────┘    └────────┬─────────┘    └────────┬─────────┘
                                               │                        │
                        ┌────────────────────────┼────────────────────────┤
                        │                        │                        │
                        ▼                        ▼                        ▼
              ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
              │  Feedback Service│    │  Product Service │    │   PostgreSQL     │
              │    Port: 8084    │    │    Port: 8085    │    │    Port: 5432    │
              │ /api/v1/feedbacks│    │ /api/v1/products │    │                  │
              └──────────────────┘    └──────────────────┘    └──────────────────┘
                                                                          │
                                                              ┌──────────────────┐
                                                              │     Redis        │
                                                              │    Port: 6379    │
                                                              └──────────────────┘
```

### 1.2 服务列表

| 服务名 | JAR 文件 | 端口 | API 路径 | 描述 |
|--------|----------|------|----------|------|
| auth | nexus-auth.jar | 8082 | /api/v1/auth | 用户认证、注册、登录 |
| user-management | nexus-user-management.jar | 8083 | /api/v1/users | 用户管理 |
| feedback | nexus-feedback.jar | 8084 | /api/v1/feedbacks | 用户反馈管理 |
| product | nexus-product.jar | 8085 | /api/v1/products | 产品管理 |

### 1.3 目录结构

```
/home/nexus/
├── app/                      # 应用部署目录
│   ├── backend/              # 后端 JAR 文件
│   │   ├── nexus-auth.jar
│   │   ├── nexus-user-management.jar
│   │   ├── nexus-feedback.jar
│   │   └── nexus-product.jar
│   ├── frontend/             # 前端构建产物
│   │   └── dist/
│   ├── config/               # 配置文件
│   │   ├── auth/
│   │   ├── user-management/
│   │   ├── feedback/
│   │   └── product/
│   ├── nexus-current.tar.gz  # 当前部署包
│   └── nexus-new.tar.gz      # 新部署包
├── backup/                   # 备份目录
│   └── backup-YYYYMMDD_HHMMSS/
├── logs/                     # 日志目录
│   ├── auth.log
│   ├── user-management.log
│   ├── feedback.log
│   └── product.log
└── deployment/               # 部署脚本
    └── scripts/
        ├── build.sh
        ├── deploy.sh
        ├── rollback.sh
        └── service.sh
```

---

## 2. 服务器配置

### 2.1 服务器信息

| 项目 | 值 |
|------|-----|
| 公网 IP | 175.178.236.183 |
| 内网 IP | 172.16.0.2 |
| 域名 | uat-nexus.cheersai.cloud |
| 操作系统 | TencentOS Server 4 |
| 部署用户 | nexus |

### 2.2 端口要求

| 端口 | 协议 | 用途 | 安全组状态 |
|------|------|------|------------|
| 22 | TCP | SSH | 已开放 |
| 80 | TCP | HTTP | 已开放 |
| 443 | TCP | HTTPS | 已开放 |
| 8082-8085 | TCP | 后端服务 | 已开放 |
| 5432 | TCP | PostgreSQL | 仅内网访问 |
| 6379 | TCP | Redis | 仅内网访问 |

### 2.3 初始化服务器

```bash
# 1. 上传脚本到服务器
scp deployment/scripts/server-setup.sh root@175.178.236.183:/home/nexus/
scp deployment/nginx/ssl-setup.sh root@175.178.236.183:/home/nexus/

# 2. 上传 SSL 证书到服务器
# 在本地执行（证书文件位于项目根目录 uat-nexus.cheersai.cloud_nginx/）
scp -r uat-nexus.cheersai.cloud_nginx root@175.178.236.183:/home/nexus/

# 3. SSH 到服务器并执行
ssh root@175.178.236.183
sudo bash /home/nexus/deployment/scripts/server-setup.sh --full

# 4. 配置 SSL 证书和 Nginx
sudo bash /home/nexus/deployment/nginx/ssl-setup.sh --all
```

---

## 3. 部署流程

### 3.1 手动部署流程

#### 步骤 1: 本地构建

**Windows 环境 (PowerShell):**

```powershell
# 在本地构建机器上执行
cd CheersAI-Nexus\deployment\scripts

# 构建（生成部署包）
.\build.ps1 -Environment uat

# 部署包位置: deployment\output\nexus-uat-YYYYMMDD_HHMMSS.zip
```

**Linux/macOS 环境 (Bash):**

```bash
# 在本地构建机器上执行
cd CheersAI-Nexus/deployment/scripts

# 构建（生成部署包）
./build.sh uat

# 部署包位置: deployment/output/nexus-uat-latest.tar.gz
```

#### 步骤 2: 上传并部署

**Windows 环境 (PowerShell):**

```powershell
# 交互式部署
.\deploy.ps1

# 非交互式部署（跳过确认）
.\deploy.ps1 -SkipBackup

# 指定部署包
.\deploy.ps1 -Package "C:\path\to\nexus-uat-20260326.zip"
```

**Linux/macOS 环境 (Bash):**

```bash
# 交互式部署
./deploy.sh

# 非交互式部署（跳过确认）
./deploy.sh --skip-backup

# 指定部署包
./deploy.sh --package=/path/to/nexus-uat-20260326.tar.gz
```

#### 步骤 3: 验证部署

```bash
# 检查服务状态
./service.sh status

# 检查日志
./service.sh logs auth

# 访问前端
curl -I https://uat-nexus.cheersai.cloud/

# 检查 API
curl https://uat-nexus.cheersai.cloud/api/v1/auth/me
```

### 3.2 Jenkins CI/CD 流程

#### 首次配置 Jenkins

```bash
# 1. 在 Jenkins Master 上创建 Agent 节点
#    - 节点名称: nexus-deploy-agent
#    - 启动方式: Launch agent via SSH
#    - 主机: 175.178.236.183
#    - 凭据: SSH 密钥 (nexus 用户)

# 2. 在部署服务器上运行 Agent 设置脚本
sudo bash /home/nexus/deployment/jenkins/jenkins-agent-setup.sh --install

# 3. 获取 Agent Secret
#    访问 Jenkins → Manage Jenkins → Nodes → nexus-deploy-agent → Configure

# 4. 创建 Pipeline 项目
#    - 选择 "Pipeline script from SCM"
#    - SCM: Git
#    - Repository: 您的仓库地址
#    - Script Path: deployment/jenkins/Jenkinsfile
```

#### Pipeline 触发条件

| 分支 | 行为 |
|------|------|
| main | 构建 → 测试 → 部署到生产 |
| develop | 构建 → 测试 → 部署到 UAT |
| 其他 | 仅构建，不部署 |

### 3.3 服务启动顺序

启动顺序很重要，后端服务有依赖关系：

```
1. nginx (先启动 Nginx)
2. nexus-auth (认证服务，其他服务依赖它验证 Token)
3. nexus-user-management (用户管理)
4. nexus-feedback (反馈服务)
5. nexus-product (产品服务)
```

---

## 4. 运维操作

### 4.1 服务管理

```bash
# 查看所有服务状态
./service.sh status

# 启动所有服务
./service.sh start

# 停止所有服务
./service.sh stop

# 重启所有服务
./service.sh restart

# 管理单个服务
./service.sh start auth
./service.sh stop user-management
./service.sh restart feedback
./service.sh restart product

# 查看实时日志
./service.sh logs auth
./service.sh logs user-management
```

### 4.2 日志管理

```bash
# 查看特定服务日志
ssh nexus@175.178.236.183 "tail -f /home/nexus/logs/auth.log"

# 查看所有服务日志
ssh nexus@175.178.236.183 "tail -f /home/nexus/logs/*.log"

# 日志轮转配置
# 查看: /etc/logrotate.d/nexus
# 保留: 14 天
# 大小: 100MB 轮转

# 清理旧日志 (手动)
ssh nexus@175.178.236.183 "find /home/nexus/logs -name '*.log.*' -mtime +30 -delete"
```

### 4.3 版本回滚

```bash
# 查看可用备份
./rollback.sh --list

# 回滚到上一个版本
./rollback.sh

# 回滚到指定版本
./rollback.sh --backup=backup-20260326_120000
```

### 4.4 Nginx 管理

```bash
# SSH 到服务器
ssh nexus@175.178.236.183

# 测试配置
sudo nginx -t

# 重载配置
sudo nginx -s reload

# 重启 Nginx
sudo systemctl restart nginx

# 查看 Nginx 状态
sudo systemctl status nginx

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 4.5 SSL 证书管理

```bash
# SSH 到服务器
ssh root@175.178.236.183

# 证书文件位置
ls -la /etc/nginx/ssl/uat-nexus.cheersai.cloud/

# 查看证书信息
openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -dates -subject

# 重新部署证书（证书到期后）
# 1. 从证书提供商获取新证书文件
# 2. 上传到服务器
scp /path/to/new/uat-nexus.cheersai.cloud.key root@175.178.236.183:/etc/nginx/ssl/uat-nexus.cheersai.cloud/privkey.pem
scp /path/to/new/uat-nexus.cheersai.cloud.bundle.crt root@175.178.236.183:/etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem

# 3. 重载 Nginx
sudo nginx -s reload
```

**证书到期处理：**
1. 从证书颁发机构获取新的证书文件（.key 和 .bundle.crt）
2. 使用上述命令上传到服务器覆盖旧文件
3. 执行 `sudo nginx -s reload` 重载配置

---

## 5. 备份与恢复

### 5.1 自动备份

部署时自动备份机制：

- 每次部署前自动备份当前版本
- 备份保留最近 5 个版本
- 备份内容包括: `backend/`、`frontend/`、`config/`

### 5.2 手动备份

```bash
# SSH 到服务器
ssh nexus@175.178.236.183

# 手动创建备份
BACKUP_NAME="manual-backup-$(date +%Y%m%d_%H%M%S)"
mkdir -p /home/nexus/backup/${BACKUP_NAME}
cp -r /home/nexus/app/backend /home/nexus/backup/${BACKUP_NAME}/
cp -r /home/nexus/app/frontend /home/nexus/backup/${BACKUP_NAME}/
cp -r /home/nexus/app/config /home/nexus/backup/${BACKUP_NAME}/

# 备份 PostgreSQL 数据库
pg_dump -U cheersai_user -d cheersai > /home/nexus/backup/${BACKUP_NAME}/database.sql

# 备份 Redis 数据 (如需要)
redis-cli -a ${REDIS_PASSWORD} SAVE
cp /var/lib/redis/dump.rdb /home/nexus/backup/${BACKUP_NAME}/

echo "备份完成: ${BACKUP_NAME}"
```

### 5.3 恢复操作

```bash
# 方案 1: 使用回滚脚本
./rollback.sh --backup=backup-YYYYMMDD_HHMMSS

# 方案 2: 手动恢复
ssh nexus@175.178.236.183
cd /home/nexus/app

# 停止服务
sudo systemctl stop nexus-product nexus-feedback nexus-user-management nexus-auth

# 恢复文件
rm -rf backend frontend config
cp -r /home/nexus/backup/backup-YYYYMMDD_HHMMSS/backend .
cp -r /home/nexus/backup/backup-YYYYMMDD_HHMMSS/frontend .
cp -r /home/nexus/backup/backup-YYYYMMDD_HHMMSS/config .

# 恢复数据库 (如需要)
psql -U cheersai_user -d cheersai < /home/nexus/backup/backup-YYYYMMDD_HHMMSS/database.sql

# 重启服务
sudo systemctl start nexus-auth
sleep 2
sudo systemctl start nexus-user-management
sleep 2
sudo systemctl start nexus-feedback
sleep 2
sudo systemctl start nexus-product
```

### 5.4 备份检查清单

| 项目 | 频率 | 保留时间 | 存储位置 |
|------|------|----------|----------|
| 应用文件 | 每次部署 | 5 个版本 | /home/nexus/backup/ |
| 数据库 | 每周 | 4 周 | /home/nexus/backup/ |
| Nginx 配置 | 每次修改 | 3 个版本 | /etc/nginx/conf.d/ |
| SSL 证书 | 证书到期前 | 2 个版本 | /etc/nginx/ssl/ |
| 系统日志 | 每天 | 30 天 | /var/log/ |

---

## 6. 故障排查

### 6.1 服务无法启动

```bash
# 1. 检查服务状态
sudo systemctl status nexus-auth -l

# 2. 查看启动错误
sudo journalctl -u nexus-auth -n 50 --no-pager

# 3. 常见问题及解决方案

# 问题: Java 未安装
# 解决: sudo yum install java-21-openjdk

# 问题: 端口被占用
# 解决: 
#   netstat -tlnp | grep 8082
#   kill <pid>

# 问题: 配置文件缺失
# 解决: 
#   ls -la /home/nexus/app/config/auth/
#   cp deployment/config/auth/* /home/nexus/app/config/auth/

# 问题: 权限不足
# 解决:
#   chown -R nexus:nexus /home/nexus/app
#   chmod -R 755 /home/nexus/app
```

### 6.2 数据库连接失败

```bash
# 1. 检查 PostgreSQL 服务
sudo systemctl status postgresql

# 2. 测试连接
psql -U cheersai_user -d cheersai -h localhost -W

# 3. 检查连接配置
grep -A5 "spring.datasource" /home/nexus/app/config/*/application-prod.yml

# 4. 检查网络连通性
telnet localhost 5432

# 5. 查看数据库日志
sudo tail -f /var/lib/pgsql/data/log/postgresql-*.log
```

### 6.3 Redis 连接失败

```bash
# 1. 检查 Redis 服务
sudo systemctl status redis

# 2. 测试连接
redis-cli -a ${REDIS_PASSWORD} ping

# 3. 查看 Redis 日志
sudo tail -f /var/log/redis/redis.log
```

### 6.4 Nginx 代理失败

```bash
# 1. 检查 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log

# 2. 测试 Nginx 配置
sudo nginx -t

# 3. 检查 upstream 是否可达
curl -v http://127.0.0.1:8082/api/v1/auth/me
curl -v http://127.0.0.1:8083/api/v1/users
curl -v http://127.0.0.1:8084/api/v1/feedbacks
curl -v http://127.0.0.1:8085/api/v1/products

# 4. 重载 Nginx 配置
sudo nginx -s reload
```

### 6.5 SSL 证书问题

```bash
# 1. 检查证书文件是否存在
ls -la /etc/nginx/ssl/uat-nexus.cheersai.cloud/

# 2. 检查证书信息
openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -dates

# 3. 检查证书与私钥是否匹配
openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -modulus | md5sum
openssl rsa -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/privkey.pem -noout -modulus | md5sum
# 两个 MD5 值应该相同

# 4. 检查 Nginx 配置中的证书路径
grep -E "ssl_certificate" /etc/nginx/conf.d/uat-nexus.cheersai.cloud.conf

# 5. 测试 SSL 连接
openssl s_client -connect uat-nexus.cheersai.cloud:443 -servername uat-nexus.cheersai.cloud

# 6. 重新部署证书
sudo bash /home/nexus/deployment/nginx/ssl-setup.sh --all
```

### 6.6 前端无法访问

```bash
# 1. 检查静态文件是否存在
ls -la /home/nexus/app/frontend/dist/

# 2. 检查 Nginx 配置
grep -A5 "root" /etc/nginx/conf.d/nexus.conf

# 3. 测试前端文件访问
curl -I https://uat-nexus.cheersai.cloud/index.html

# 4. 检查前端构建产物
#    必需文件: index.html, assets/
ls -la /home/nexus/app/frontend/dist/
```

### 6.7 常见问题汇总

| 问题现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| 502 Bad Gateway | 后端服务未启动 | `systemctl start nexus-*` |
| 504 Gateway Timeout | 服务响应慢或挂起 | 重启对应服务，检查日志 |
| 401 Unauthorized | Token 过期或无效 | 重新登录 |
| 403 Forbidden | 权限不足 | 检查用户角色 |
| 502 SSL handshake failed | SSL 证书问题 | 检查证书配置 |
| 数据库连接失败 | PostgreSQL 未启动 | `systemctl start postgresql` |
| 前端样式丢失 | 静态资源未加载 | 检查 Nginx root 配置 |
| CORS 错误 | 跨域配置问题 | 检查 Nginx 代理头 |

---

## 7. 监控与告警

### 7.1 健康检查

```bash
# 服务健康检查
curl http://127.0.0.1:8082/actuator/health  # auth
curl http://127.0.0.1:8083/actuator/health  # user-management
curl http://127.0.0.1:8084/actuator/health  # feedback
curl http://127.0.0.1:8085/actuator/health  # product

# Nginx 健康检查
curl https://uat-nexus.cheersai.cloud/health

# 全链路检查
curl -sf https://uat-nexus.cheersai.cloud/api/v1/auth/me && echo "API OK"
```

### 7.2 资源使用监控

```bash
# SSH 到服务器
ssh nexus@175.178.236.183

# CPU 和内存使用
top -u nexus

# Java 进程内存
jps -l
jstat -gc <pid>

# 磁盘使用
df -h

# 网络连接
ss -s

# 端口监听
netstat -tlnp
```

### 7.3 日志监控

```bash
# 实时错误日志
ssh nexus@175.178.236.183 "grep -i error /home/nexus/logs/*.log | tail -50"

# 访问日志统计
ssh nexus@175.178.236.183 "tail -1000 /var/log/nginx/access.log | awk '{print $7}' | sort | uniq -c | sort -rn | head -20"

# API 响应时间
ssh nexus@175.178.236.183 "tail -1000 /var/log/nginx/access.log | awk '{print $7, $NF}' | grep api | head -10"
```

---

## 8. 安全配置

### 8.1 服务器安全

```bash
# 1. 配置 SSH 密钥登录 (避免密码登录)
# 在本地生成密钥
ssh-keygen -t ed25519 -C "nexus-deploy-key"
ssh-copy-id -i ~/.ssh/id_ed25519.pub nexus@175.178.236.183

# 2. 禁用密码登录
sudo sed -i 's/PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart sshd

# 3. 配置防火墙
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# 4. 安装 fail2ban
sudo yum install -y fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 8.2 应用安全

```bash
# 1. 设置环境变量 (不要在配置文件中明文存储密码)
# 在 /etc/environment 或 systemd service 中设置

# 2. 配置敏感文件权限
chmod 600 /home/nexus/app/config/*/application-prod.yml
chown nexus:nexus /home/nexus/app/config -R

# 3. 定期更新软件
sudo yum update --security
```

### 8.3 SSL/TLS 安全

```bash
# 1. 检查 SSL 等级
curl -sI https://uat-nexus.cheersai.cloud | grep -i strict-transport

# 2. SSL 测试
# 访问 https://www.ssllabs.com/ssltest/analyze.html?d=uat-nexus.cheersai.cloud

# 3. 检查证书到期时间
openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -dates

# 4. 设置证书到期监控（建议在证书到期前 30 天续期）
# 可以使用以下命令检查
ssh root@175.178.236.183 "openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -enddate"
```

---

## 9. 附录

### 9.1 快速命令参考

**Windows (PowerShell):**

```powershell
# 构建
.\build.ps1 -Environment uat

# 部署
.\deploy.ps1

# 部署（跳过确认）
.\deploy.ps1 -SkipBackup

# 查看 SSL 证书信息
ssh root@175.178.236.183 "openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -enddate"
```

**Linux/macOS (Bash):**

```bash
# 部署
./deploy.sh

# 回滚
./rollback.sh

# 服务管理
./service.sh start|stop|restart|status|logs [service]

# 查看日志
ssh nexus@175.178.236.183 "tail -f /home/nexus/logs/*.log"

# 重启 Nginx
sudo nginx -s reload

# 查看 SSL 证书信息
ssh root@175.178.236.183 "openssl x509 -in /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem -noout -enddate"
```

### 9.2 联系人

| 角色 | 职责 | 联系方式 |
|------|------|----------|
| 运维工程师 | 部署、监控 | ops@cheersai.cloud |
| 开发团队 | 应用问题 | dev@cheersai.cloud |
| DBA | 数据库 | dba@cheersai.cloud |

### 9.3 文档更新记录

| 日期 | 版本 | 更新内容 | 作者 |
|------|------|----------|------|
| 2026-03-26 | 1.0.0 | 初始版本 | Sisyphus |

---

*本手册最后更新于 2026-03-26*
