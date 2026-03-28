#!/bin/bash
#==============================================================================
# 服务器初始化脚本
# 在部署服务器首次配置时运行
# 
# 使用方法: ./server-setup.sh [--full]
#==============================================================================

set -e

# 配置
SERVER_USER="nexus"
APP_DIR="/home/nexus/app"
BACKUP_DIR="/home/nexus/backup"
LOG_DIR="/home/nexus/logs"
CONFIG_DIR="/home/nexus/config"
DEPLOY_SCRIPTS_DIR="/home/nexus/deployment/scripts"

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 显示帮助
usage() {
    cat << EOF
使用方法: $0 [选项]

选项:
  --full        完整安装（安装所有软件包）
  --basic       仅创建目录和配置（假设软件已安装）
  -h, --help    显示帮助

示例:
  $0 --full     # 完整安装
  $0 --basic    # 仅创建目录
EOF
    exit 1
}

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 sudo 或以 root 用户身份运行此脚本"
        exit 1
    fi
}

# 安装基础软件
install_packages() {
    log_info "========== 安装基础软件 =========="
    
    if command -v yum &> /dev/null; then
        # TencentOS / CentOS / RHEL
        
        # Java 21
        yum install -y java-21-openjdk java-21-openjdk-devel
        
        # Maven
        yum install -y maven
        
        # Node.js 20.x
        curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
        yum install -y nodejs
        
        # Nginx
        yum install -y nginx
        
        # Certbot
        yum install -y certbot python3-certbot-nginx
        
        # PostgreSQL 16 (使用自带源)
        yum install -y postgresql16 postgresql16-server postgresql16-contrib
        
        # Redis 7 (使用自带源)
        yum install -y redis
        
        # 其他工具
        yum install -y git wget curl unzip htop iftop iotop lsof net-tools
        
    elif command -v apt &> /dev/null; then
        # Debian / Ubuntu
        apt update
        
        # Java 21
        apt install -y openjdk-21-jdk maven
        
        # Node.js
        curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
        apt install -y nodejs
        
        # Nginx
        apt install -y nginx certbot python3-certbot-nginx
        
        # PostgreSQL 16
        apt install -y postgresql-16 postgresql-client-16
        
        # Redis
        apt install -y redis-server
        
        # 其他工具
        apt install -y git wget curl unzip htop iftop iotop lsof net-tools
    fi
    
    log_success "软件包安装完成"
}

# 创建用户
create_user() {
    log_info "========== 创建部署用户 =========="
    
    if id "${SERVER_USER}" &>/dev/null; then
        log_warn "用户 ${SERVER_USER} 已存在"
    else
        useradd -m -s /bin/bash ${SERVER_USER}
        echo "${SERVER_USER}:Nexus2026!" | chpasswd
        log_success "用户 ${SERVER_USER} 已创建"
    fi
    
    # 配置 sudo（密码less sudo）
    echo "${SERVER_USER} ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/${SERVER_USER}
    chmod 440 /etc/sudoers.d/${SERVER_USER}
}

# 配置 PostgreSQL 16
configure_postgresql() {
    log_info "========== 配置 PostgreSQL 16 =========="
    
    local PG_PASSWORD="cheersai"
    local PGSQL_DATA_DIR="/var/lib/pgsql/16/data"
    
    if command -v yum &> /dev/null; then
        # 初始化 PostgreSQL
        if [ ! -d "${PGSQL_DATA_DIR}" ]; then
            /usr/pgsql-16/bin/postgresql-16-setup initdb
        fi
        
        # 配置 PostgreSQL 允许 SSH 隧道连接
        cat > /var/lib/pgsql/16/data/pg_hba.conf << 'EOF'
# TYPE  DATABASE        USER            ADDRESS                 METHOD
# 本地连接
local   all             all                                     trust
# IPv4 本地回环
host    all             all             127.0.0.1/32            trust
# IPv6 本地回环
host    all             all             ::1/128                 trust
# 允许来自本地网络的连接（通过 SSH 隧道）
host    all             all             172.16.0.0/16           md5
host    all             all             10.0.0.0/8              md5
host    all             all             192.168.0.0/16           md5
# IPv4 本地网络
host    all             all             0.0.0.0/0               md5
EOF
        
        # 配置 PostgreSQL 监听地址
        sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /var/lib/pgsql/16/data/postgresql.conf
        sed -i "s/listen_addresses = 'localhost'/listen_addresses = '*'/" /var/lib/pgsql/16/data/postgresql.conf
        
        # 设置 max_connections
        sed -i "s/#max_connections = 100/max_connections = 200/" /var/lib/pgsql/16/data/postgresql.conf
        
        # 启动 PostgreSQL
        systemctl enable postgresql-16
        systemctl start postgresql-16
        
        # 设置 postgres 用户密码
        su - postgres -c "psql -c \"ALTER USER postgres WITH PASSWORD '${PG_PASSWORD}';\""
        
        # 创建 cheersai 数据库
        su - postgres -c "psql -c \"DROP DATABASE IF EXISTS cheersai;\""
        su - postgres -c "psql -c \"CREATE DATABASE cheersai;\""
        su - postgres -c "psql -c \"ALTER DATABASE cheersai OWNER TO postgres;\""
        
        # 创建 cheersai_user（数据库应用用户）
        su - postgres -c "psql -c \"DROP USER IF EXISTS cheersai_user;\""
        su - postgres -c "psql -c \"CREATE USER cheersai_user WITH PASSWORD '${PG_PASSWORD}';\""
        su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE cheersai TO cheersai_user;\""
        su - postgres -c "psql -c \"ALTER USER cheersai_user CREATEDB;\""
        
        log_success "PostgreSQL 16 配置完成"
        log_info "  - 数据库: cheersai"
        log_info "  - 应用用户: cheersai_user / ${PG_PASSWORD}"
        log_info "  - 超级用户: postgres / ${PG_PASSWORD}"
        log_info "  - 端口: 5432"
        log_info "  - SSH 隧道连接: 支持 (md5 认证)"
        
    elif command -v apt &> /dev/null; then
        # Debian/Ubuntu PostgreSQL 配置
        local PG_CONF="/etc/postgresql/16/main/postgresql.conf"
        local PG_HBA="/etc/postgresql/16/main/pg_hba.conf"
        
        # 配置监听地址
        sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" ${PG_CONF}
        sed -i "s/listen_addresses = 'localhost'/listen_addresses = '*'/" ${PG_CONF}
        
        # 配置 SSH 隧道连接
        cat > ${PG_HBA} << 'EOF'
# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   all             all                                     trust
host    all             all             127.0.0.1/32            trust
host    all             all             ::1/128                 trust
host    all             all             0.0.0.0/0               md5
EOF
        
        # 重启 PostgreSQL
        systemctl enable postgresql@16-main
        systemctl restart postgresql@16-main
        
        # 设置密码
        su - postgres -c "psql -c \"ALTER USER postgres WITH PASSWORD '${PG_PASSWORD}';\""
        su - postgres -c "psql -c \"DROP DATABASE IF EXISTS cheersai;\""
        su - postgres -c "psql -c \"CREATE DATABASE cheersai;\""
        su - postgres -c "psql -c \"CREATE USER cheersai_user WITH PASSWORD '${PG_PASSWORD}';\""
        su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE cheersai TO cheersai_user;\""
        
        log_success "PostgreSQL 16 配置完成"
    fi
}

# 配置 Redis 7
configure_redis() {
    log_info "========== 配置 Redis 7 =========="
    
    local REDIS_PASSWORD="cheersai"
    
    if command -v yum &> /dev/null; then
        # 配置 Redis 允许 SSH 隧道连接
        sed -i "s/# bind 127.0.0.1/bind 0.0.0.0/" /etc/redis.conf
        sed -i "s/bind 127.0.0.1/bind 0.0.0.0/" /etc/redis.conf
        
        # 设置 Redis 密码
        sed -i "s/# requirepass foobared/requirepass ${REDIS_PASSWORD}/" /etc/redis.conf
        sed -i "s/requirepass foobared/requirepass ${REDIS_PASSWORD}/" /etc/redis.conf
        
        # 关闭保护模式（允许远程连接通过密码认证）
        sed -i "s/protected-mode yes/protected-mode no/" /etc/redis.conf
        
        # 设置持久化
        sed -i "s/# appendonly no/appendonly yes/" /etc/redis.conf
        
        # 启动 Redis
        systemctl enable redis
        systemctl restart redis
        
        # 验证 Redis 连接
        if redis-cli -a "${REDIS_PASSWORD}" ping | grep -q PONG; then
            log_success "Redis 7 配置完成"
            log_info "  - 密码: ${REDIS_PASSWORD}"
            log_info "  - 端口: 6379"
            log_info "  - SSH 隧道连接: 支持"
        else
            log_warn "Redis 连接验证失败，请检查配置"
        fi
        
    elif command -v apt &> /dev/null; then
        local REDIS_CONF="/etc/redis/redis.conf"
        
        sed -i "s/# bind 127.0.0.1/bind 0.0.0.0/" ${REDIS_CONF}
        sed -i "s/bind 127.0.0.1/bind 0.0.0.0/" ${REDIS_CONF}
        sed -i "s/# requirepass foobared/requirepass ${REDIS_PASSWORD}/" ${REDIS_CONF}
        sed -i "s/protected-mode yes/protected-mode no/" ${REDIS_CONF}
        
        systemctl enable redis-server
        systemctl restart redis-server
        
        log_success "Redis 7 配置完成"
    fi
    
    log_info "  - SSH 隧道连接方法: ssh -L 6379:localhost:6379 user@175.178.236.183"
}

# 创建目录结构
create_dirs() {
    log_info "========== 创建目录结构 =========="
    
    local dirs=(
        "${APP_DIR}"
        "${APP_DIR}/backend"
        "${APP_DIR}/frontend"
        "${APP_DIR}/config"
        "${APP_DIR}/config/auth"
        "${APP_DIR}/config/user-management"
        "${APP_DIR}/config/feedback"
        "${APP_DIR}/config/product"
        "${APP_DIR}/config/membership"
        "${APP_DIR}/config/auditlog"
        "${BACKUP_DIR}"
        "${LOG_DIR}"
        "${DEPLOY_SCRIPTS_DIR}"
    )
    
    for dir in "${dirs[@]}"; do
        mkdir -p "${dir}"
        chown -R ${SERVER_USER}:${SERVER_USER} "${dir}"
        chmod 755 "${dir}"
        log_info "✓ ${dir}"
    done
    
    log_success "目录结构创建完成"
}

# 配置防火墙
setup_firewall() {
    log_info "========== 配置防火墙 =========="
    
    # 检查 firewalld
    if systemctl is-active --quiet firewalld; then
        firewall-cmd --permanent --add-service=http
        firewall-cmd --permanent --add-service=https
        firewall-cmd --permanent --add-port=8082-8087/tcp
        firewall-cmd --permanent --add-port=5432/tcp    # PostgreSQL
        firewall-cmd --permanent --add-port=6379/tcp     # Redis
        firewall-cmd --reload
        log_success "firewalld 已配置"
    elif systemctl is-active --quiet ufw; then
        ufw allow http
        ufw allow https
        ufw allow 8082:8087/tcp
        ufw allow 5432/tcp
        ufw allow 6379/tcp
        ufw reload
        log_success "ufw 已配置"
    else
        log_warn "未检测到活跃的防火墙服务"
    fi
}

# 部署 systemd 服务文件
deploy_services() {
    log_info "========== 部署 systemd 服务文件 =========="
    
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local systemd_dir="${script_dir}/../systemd"
    
    if [ -d "${systemd_dir}" ]; then
        cp ${systemd_dir}/*.service /etc/systemd/system/
        systemctl daemon-reload
        
        # 设置开机自启
        for svc in nexus-auth nexus-user-management nexus-feedback nexus-product; do
            systemctl enable ${svc}
            log_info "✓ ${svc} 已设置为开机自启"
        done
        
        log_success "systemd 服务已部署"
    else
        log_warn "未找到 systemd 服务文件"
    fi
}

# 部署 Nginx 配置
deploy_nginx() {
    log_info "========== 部署 Nginx 配置 =========="
    
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local nginx_conf="${script_dir}/../nginx/nexus.conf"
    
    if [ -f "${nginx_conf}" ]; then
        cp "${nginx_conf}" /etc/nginx/conf.d/nexus.conf
        chown root:root /etc/nginx/conf.d/nexus.conf
        chmod 644 /etc/nginx/conf.d/nexus.conf
        
        # 测试配置
        nginx -t
        log_success "Nginx 配置已部署"
    else
        log_warn "未找到 Nginx 配置文件"
    fi
    
    # 创建 Let's Encrypt webroot
    mkdir -p /var/www/letsencrypt
    chown -R nginx:nginx /var/www/letsencrypt 2>/dev/null || \
    chown -R www-data:www-data /var/www/letsencrypt 2>/dev/null || \
    chown -R nobody:nobody /var/www/letsencrypt
}

# 创建日志轮转配置
setup_logrotate() {
    log_info "========== 配置日志轮转 =========="
    
    cat > /etc/logrotate.d/nexus << 'EOF'
/home/nexus/logs/*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0644 nexus nexus
    sharedscripts
    postrotate
        systemctl reload nexus-auth nexus-user-management nexus-feedback nexus-product > /dev/null 2>&1 || true
    endscript
}
EOF
    
    log_success "日志轮转已配置"
}

# 验证安装
verify_installation() {
    log_info "========== 验证安装 =========="
    
    local errors=0
    
    # 检查 Java
    if java -version 2>&1 | head -1 | grep -q "21"; then
        log_success "✓ Java 21"
    else
        log_error "✗ Java 21 未正确安装"
        errors=$((errors + 1))
    fi
    
    # 检查 Maven
    if mvn -version &>/dev/null; then
        log_success "✓ Maven"
    else
        log_error "✗ Maven 未正确安装"
        errors=$((errors + 1))
    fi
    
    # 检查 Node.js
    if node -v &>/dev/null; then
        log_success "✓ Node.js $(node -v)"
    else
        log_error "✗ Node.js 未正确安装"
        errors=$((errors + 1))
    fi
    
    # 检查 Nginx
    if nginx -v &>/dev/null; then
        log_success "✓ Nginx"
    else
        log_error "✗ Nginx 未正确安装"
        errors=$((errors + 1))
    fi
    
    # 检查目录权限
    if [ -w "${APP_DIR}" ]; then
        log_success "✓ APP_DIR 可写"
    else
        log_error "✗ APP_DIR 不可写"
        errors=$((errors + 1))
    fi
    
    if [ $errors -eq 0 ]; then
        log_success "所有检查通过!"
    else
        log_error "$errors 个检查失败"
        return 1
    fi
}

# 主函数
main() {
    local mode="basic"
    
    for arg in "$@"; do
        case $arg in
            --full)
                mode="full"
                ;;
            --basic)
                mode="basic"
                ;;
            -h|--help)
                usage
                ;;
            *)
                log_error "未知参数: $arg"
                usage
                ;;
        esac
    done
    
    echo ""
    echo "========================================"
    echo "  CheersAI-Nexus 服务器初始化脚本"
    echo "  模式: ${mode}"
    echo "========================================"
    echo ""
    
    check_root
    
    if [ "${mode}" = "full" ]; then
        install_packages
        configure_postgresql
        configure_redis
    fi
    
    create_user
    create_dirs
    deploy_nginx
    deploy_services
    setup_logrotate
    verify_installation
    
    echo ""
    echo "========================================"
    echo "  服务器初始化完成!"
    echo "========================================"
    echo ""
    echo "下一步操作:"
    echo "  1. 配置 SSL 证书: cd /home/nexus/deployment/nginx && ./ssl-setup.sh --all"
    echo "  2. 部署应用: 将 deployment/ 目录复制到 /home/nexus/deployment/scripts/"
    echo "  3. 构建并部署: ./build.sh && ./deploy.sh"
    echo ""
}

main "$@"
