#!/bin/bash
#==============================================================================
# SSL 证书部署脚本
# 
# 使用已有 SSL 证书配置 Nginx HTTPS
# 证书文件位于: uat-nexus.cheersai.cloud_nginx/
#
# 使用方法: ./ssl-setup.sh [--deploy|--nginx|--all]
#==============================================================================

set -e

# 配置
DOMAIN="uat-nexus.cheersai.cloud"
CERT_DIR="/etc/nginx/ssl/${DOMAIN}"
CERT_SOURCE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/uat-nexus.cheersai.cloud_nginx"

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
  --deploy    仅部署 SSL 证书到服务器
  --nginx     仅配置 Nginx
  --all       完整部署（证书 + Nginx）
  -h, --help  显示帮助

示例:
  $0 --all    # 完整部署
EOF
    exit 1
}

# 检查证书文件
check_cert_files() {
    log_info "========== 检查证书文件 =========="
    
    local missing=0
    
    if [ ! -f "${CERT_SOURCE}/${DOMAIN}.key" ]; then
        log_error "缺少私钥文件: ${CERT_SOURCE}/${DOMAIN}.key"
        missing=1
    else
        log_success "找到私钥: ${DOMAIN}.key"
    fi
    
    if [ ! -f "${CERT_SOURCE}/${DOMAIN}_bundle.crt" ]; then
        log_error "缺少证书文件: ${CERT_SOURCE}/${DOMAIN}_bundle.crt"
        missing=1
    else
        log_success "找到证书: ${DOMAIN}_bundle.crt"
    fi
    
    if [ $missing -eq 1 ]; then
        log_error "证书文件缺失，请确保 uat-nexus.cheersai.cloud_nginx/ 目录下有完整证书"
        exit 1
    fi
}

# 部署证书到服务器
deploy_cert() {
    log_info "========== 部署 SSL 证书 =========="
    
    # 本地检查
    check_cert_files
    
    # SSH 到服务器创建目录并上传证书
    ssh root@175.178.236.183 "
        set -e
        mkdir -p ${CERT_DIR}
        chmod 755 ${CERT_DIR}
        
        # 创建 Nginx 续期钩子目录
        mkdir -p /etc/letsencrypt/renewal-hooks/post
        chmod 755 /etc/letsencrypt/renewal-hooks/post
    "
    
    # 上传私钥
    log_info "上传私钥..."
    scp "${CERT_SOURCE}/${DOMAIN}.key" root@175.178.236.183:${CERT_DIR}/privkey.pem
    
    # 上传证书
    log_info "上传证书..."
    scp "${CERT_SOURCE}/${DOMAIN}_bundle.crt" root@175.178.236.183:${CERT_DIR}/fullchain.pem
    
    # 在服务器设置权限
    ssh root@175.178.236.183 "
        set -e
        chmod 644 ${CERT_DIR}/fullchain.pem
        chmod 600 ${CERT_DIR}/privkey.pem
        chown root:root ${CERT_DIR}/*.pem
        ls -la ${CERT_DIR}/
    "
    
    log_success "SSL 证书已部署到 ${CERT_DIR}"
}

# 配置 Nginx SSL
setup_nginx_ssl() {
    log_info "========== 配置 Nginx SSL =========="
    
    local SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local NGINX_CONF="/etc/nginx/conf.d/${DOMAIN}.conf"
    
    # 创建 Nginx 配置（仅 HTTPS server block，不做 HTTP 重定向）
    ssh root@175.178.236.183 "cat > ${NGINX_CONF}" << 'NGINX_EOF'
# CheersAI-Nexus HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name uat-nexus.cheersai.cloud;

    #---------------------------------------
    # SSL 证书配置
    #---------------------------------------
    ssl_certificate /etc/nginx/ssl/uat-nexus.cheersai.cloud/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/uat-nexus.cheersai.cloud/privkey.pem;
    
    #---------------------------------------
    # SSL 安全配置
    #---------------------------------------
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    ssl_session_tickets off;

    #---------------------------------------
    # 安全头
    #---------------------------------------
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    #---------------------------------------
    # Gzip 压缩
    #---------------------------------------
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml application/json application/javascript application/rss+xml application/atom+xml image/svg+xml application/xml;
    gzip_min_length 1000;

    #---------------------------------------
    # 前端静态文件
    #---------------------------------------
    root /home/nexus/app/frontend/dist;
    index index.html;

    # 静态资源缓存策略
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # HTML 文件不缓存
    location ~* \.html$ {
        expires -1;
        add_header Cache-Control "no-store, no-cache, must-revalidate";
    }

    #---------------------------------------
    # API 代理
    #---------------------------------------
    location /api/v1/auth {
        proxy_pass http://127.0.0.1:8082;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /api/v1/users {
        proxy_pass http://127.0.0.1:8083;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /api/v1/feedbacks {
        proxy_pass http://127.0.0.1:8084;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /api/v1/products {
        proxy_pass http://127.0.0.1:8085;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    #---------------------------------------
    # 前端路由 (SPA 支持)
    #---------------------------------------
    location / {
        try_files $uri $uri/ /index.html;
    }

    #---------------------------------------
    # 健康检查端点
    #---------------------------------------
    location /health {
        access_log off;
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }

    #---------------------------------------
    # 错误页面
    #---------------------------------------
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
NGINX_EOF
    
    # 测试 Nginx 配置
    log_info "测试 Nginx 配置..."
    ssh root@175.178.236.183 "nginx -t"
    
    # 重载 Nginx
    log_info "重载 Nginx..."
    ssh root@175.178.236.183 "nginx -s reload || nginx"
    
    log_success "Nginx SSL 配置完成"
}

# 验证部署
verify_ssl() {
    log_info "========== 验证 SSL 配置 =========="
    
    local cert_info
    cert_info=$(ssh root@175.178.236.183 "openssl s_client -servername ${DOMAIN} -connect ${DOMAIN}:443 </dev/null 2>/dev/null | openssl x509 -noout -dates -subject 2>/dev/null" || echo "无法获取证书信息")
    
    log_info "证书信息:"
    echo "${cert_info}"
    
    log_success "SSL 验证完成"
}

# 主函数
main() {
    local action=""
    
    for arg in "$@"; do
        case $arg in
            --deploy)
                action="deploy"
                ;;
            --nginx)
                action="nginx"
                ;;
            --all)
                action="all"
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
    
    if [ -z "${action}" ]; then
        echo ""
        echo "========================================"
        echo "  SSL 证书部署脚本"
        echo "========================================"
        echo ""
        echo "可用选项:"
        echo "  --deploy  仅部署 SSL 证书"
        echo "  --nginx   仅配置 Nginx"
        echo "  --all     完整部署"
        echo ""
        read -p "请选择操作 [all]: " action
        action=${action:-all}
    fi
    
    echo ""
    echo "========================================"
    echo "  SSL 证书部署"
    echo "  操作: ${action}"
    echo "========================================"
    echo ""
    
    case $action in
        deploy)
            check_cert_files
            deploy_cert
            ;;
        nginx)
            setup_nginx_ssl
            ;;
        all)
            check_cert_files
            deploy_cert
            setup_nginx_ssl
            verify_ssl
            ;;
    esac
    
    log_success "SSL 配置完成!"
}

main "$@"
