#!/bin/bash
#==============================================================================
# CheersAI-Nexus 部署脚本
# 将构建产物部署到目标服务器
# 
# 使用方法: ./deploy.sh [选项]
# 示例: 
#   ./deploy.sh                      # 交互式部署
#   ./deploy.sh --package=nexus-uat-latest.tar.gz  # 使用指定包
#   ./deploy.sh --skip-backup        # 跳过备份
#   ./deploy.sh --skip-restart       # 仅上传不重启
#==============================================================================

set -e
set -u

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEPLOY_DIR="${PROJECT_ROOT}"
DEFAULT_APP_DIR="/home/nexus/app"
DEFAULT_BACKUP_DIR="/home/nexus/backup"
DEFAULT_LOG_DIR="/home/nexus/logs"
MAX_BACKUP_COUNT=5

# 服务器配置
SERVER_HOST="175.178.236.183"
SERVER_USER="nexus"

# 部署包路径（默认在本地构建目录）
PACKAGE_PATH=""
SKIP_BACKUP=false
SKIP_RESTART=false
DRY_RUN=false

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 显示使用方法
usage() {
    cat << EOF
使用方法: $0 [选项]

选项:
  --package=<文件>      指定部署包路径（默认为 deployment/output 目录下的最新包）
  --server=<主机>       目标服务器地址 (默认: ${SERVER_HOST})
  --user=<用户>         SSH 用户 (默认: ${SERVER_USER})
  --app-dir=<目录>      应用部署目录 (默认: ${DEFAULT_APP_DIR})
  --skip-backup         跳过备份步骤
  --skip-restart        仅上传文件，不重启服务
  --dry-run             模拟运行，不执行实际操作
  -h, --help            显示帮助信息

示例:
  $0                                    # 使用最新包部署
  $0 --package=./nexus-prod.tar.gz     # 使用指定包
  $0 --skip-backup --skip-restart       # 仅上传文件
EOF
    exit 1
}

# 解析命令行参数
parse_args() {
    for arg in "$@"; do
        case $arg in
            --package=*)
                PACKAGE_PATH="${arg#*=}"
                ;;
            --server=*)
                SERVER_HOST="${arg#*=}"
                ;;
            --user=*)
                SERVER_USER="${arg#*=}"
                ;;
            --app-dir=*)
                DEFAULT_APP_DIR="${arg#*=}"
                ;;
            --skip-backup)
                SKIP_BACKUP=true
                ;;
            --skip-restart)
                SKIP_RESTART=true
                ;;
            --dry-run)
                DRY_RUN=true
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
}

# 查找部署包
find_package() {
    if [ -z "${PACKAGE_PATH}" ]; then
        # 查找最新的部署包
        PACKAGE_PATH=$(ls -t "${DEPLOY_DIR}/output/"nexus-*.tar.gz 2>/dev/null | head -1)
        if [ -z "${PACKAGE_PATH}" ]; then
            log_error "未找到部署包，请先运行 build.sh 构建"
            exit 1
        fi
    fi
    
    if [ ! -f "${PACKAGE_PATH}" ]; then
        log_error "部署包不存在: ${PACKAGE_PATH}"
        exit 1
    fi
    
    log_info "使用部署包: ${PACKAGE_PATH}"
}

# 备份当前版本
backup_current() {
    if [ "${SKIP_BACKUP}" = true ]; then
        log_info "跳过备份步骤"
        return
    fi
    
    log_info "========== 备份当前版本 =========="
    
    local backup_name="backup-$(date +%Y%m%d_%H%M%S)"
    local backup_path="${DEFAULT_BACKUP_DIR}/${backup_name}"
    
    if [ "${DRY_RUN}" = true ]; then
        log_info "[DRY-RUN] 将创建备份: ${backup_path}"
        return
    fi
    
    # SSH 执行备份
    ssh ${SERVER_USER}@${SERVER_HOST} "
        set -e
        mkdir -p ${DEFAULT_BACKUP_DIR}
        
        # 检查当前应用是否存在
        if [ -d '${DEFAULT_APP_DIR}/backend' ] || [ -d '${DEFAULT_APP_DIR}/frontend' ]; then
            echo '创建备份...'
            mkdir -p ${backup_path}
            
            # 备份 backend
            if [ -d '${DEFAULT_APP_DIR}/backend' ]; then
                cp -r '${DEFAULT_APP_DIR}/backend' '${backup_path}/'
            fi
            
            # 备份 frontend
            if [ -d '${DEFAULT_APP_DIR}/frontend' ]; then
                cp -r '${DEFAULT_APP_DIR}/frontend' '${backup_path}/'
            fi
            
            # 备份配置
            if [ -d '${DEFAULT_APP_DIR}/config' ]; then
                cp -r '${DEFAULT_APP_DIR}/config' '${backup_path}/'
            fi
            
            echo '备份完成: ${backup_path}'
            
            # 清理旧备份，保留最近 N 个
            cd ${DEFAULT_BACKUP_DIR}
            ls -dt backup-* | tail -n +$((MAX_BACKUP_COUNT + 1)) | xargs -r rm -rf
            echo '旧备份清理完成'
        else
            echo '当前应用目录为空，跳过备份'
        fi
    "
    
    log_success "备份完成"
}

# 上传部署包
upload_package() {
    log_info "========== 上传部署包 =========="
    
    if [ "${DRY_RUN}" = true ]; then
        log_info "[DRY-RUN] 将上传: ${PACKAGE_PATH} -> ${SERVER_USER}@${SERVER_HOST}:${DEFAULT_APP_DIR}/"
        return
    fi
    
    # 上传并解压
    scp "${PACKAGE_PATH}" ${SERVER_USER}@${SERVER_HOST}:${DEFAULT_APP_DIR}/nexus-new.tar.gz
    
    ssh ${SERVER_USER}@${SERVER_HOST} "
        set -e
        cd ${DEFAULT_APP_DIR}
        
        echo '解压部署包...'
        tar -xzf nexus-new.tar.gz
        
        # 备份旧的 MD5 校验文件
        if [ -f 'nexus-current.tar.gz.md5' ]; then
            cp nexus-current.tar.gz.md5 nexus-old.tar.gz.md5 2>/dev/null || true
        fi
        
        # 更新当前包引用
        mv nexus-new.tar.gz nexus-current.tar.gz
        
        # 重新计算 MD5
        md5sum nexus-current.tar.gz > nexus-current.tar.gz.md5
        
        echo '部署包解压完成'
    "
    
    log_success "部署包上传完成"
}

# 重启服务
restart_services() {
    if [ "${SKIP_RESTART}" = true ]; then
        log_info "跳过服务重启步骤"
        return
    fi
    
    log_info "========== 重启服务 =========="
    
    if [ "${DRY_RUN}" = true ]; then
        log_info "[DRY-RUN] 将重启以下服务:"
        log_info "  - nexus-auth"
        log_info "  - nexus-user-management"
        log_info "  - nexus-feedback"
        log_info "  - nexus-product"
        log_info "  - nexus-membership"
        log_info "  - nexus-auditlog"
        log_info "  - nginx"
        return
    fi
    
    ssh ${SERVER_USER}@${SERVER_HOST} "
        set -e
        
        echo '停止旧服务...'
        systemctl stop nexus-auditlog 2>/dev/null || true
        systemctl stop nexus-membership 2>/dev/null || true
        systemctl stop nexus-product 2>/dev/null || true
        systemctl stop nexus-feedback 2>/dev/null || true
        systemctl stop nexus-user-management 2>/dev/null || true
        systemctl stop nexus-auth 2>/dev/null || true
        
        echo '等待服务停止...'
        sleep 3
        
        echo '启动新服务...'
        systemctl start nexus-auth
        sleep 2
        systemctl start nexus-user-management
        sleep 2
        systemctl start nexus-feedback
        sleep 2
        systemctl start nexus-product
        sleep 2
        systemctl start nexus-membership
        sleep 2
        systemctl start nexus-auditlog
        sleep 2
        
        echo '重启 Nginx...'
        nginx -t && nginx -s reload || nginx
        
        echo '检查服务状态...'
        for svc in nexus-auth nexus-user-management nexus-feedback nexus-product nexus-membership nexus-auditlog; do
            if systemctl is-active --quiet \$svc; then
                echo \"✓ \$svc is running\"
            else
                echo \"✗ \$svc is NOT running\"
            fi
        done
        
        echo '部署完成!'
    "
    
    log_success "服务重启完成"
}

# 验证部署
verify_deployment() {
    log_info "========== 验证部署 =========="
    
    if [ "${DRY_RUN}" = true ]; then
        log_info "[DRY-RUN] 跳过验证"
        return
    fi
    
    local domain="uat-nexus.cheersai.cloud"
    local retries=3
    local success=false
    
    for i in $(seq 1 $retries); do
        log_info "尝试 $i/$retries: 检查前端页面..."
        if curl -sf "https://${domain}/" > /dev/null; then
            success=true
            break
        fi
        sleep 2
    done
    
    if [ "$success" = true ]; then
        log_success "前端页面验证通过"
    else
        log_warn "前端页面验证失败，请检查 Nginx 配置"
    fi
    
    # 检查 API 端点
    log_info "检查 API 端点..."
    for endpoint in "/api/v1/auth/me" "/api/v1/users" "/api/v1/products" "/api/v1/feedbacks" "/api/v1/membership/plans" "/api/v1/audit-logs"; do
        if curl -sf -o /dev/null -w "%{http_code}" "https://${domain}${endpoint}" | grep -q "401\|200\|404"; then
            log_success "✓ ${endpoint} 可访问"
        else
            log_warn "✗ ${endpoint} 无法访问"
        fi
    done
}

# 显示部署摘要
show_summary() {
    log_info "========== 部署摘要 =========="
    echo ""
    echo "  部署包:   ${PACKAGE_PATH}"
    echo "  服务器:   ${SERVER_USER}@${SERVER_HOST}"
    echo "  应用目录: ${DEFAULT_APP_DIR}"
    echo "  备份目录: ${DEFAULT_BACKUP_DIR}"
    echo ""
    
    if [ "${DRY_RUN}" = true ]; then
        echo "  [DRY-RUN 模式 - 未执行实际操作]"
    fi
    
    echo "========================================"
}

# 主函数
main() {
    parse_args "$@"
    
    echo ""
    echo "========================================"
    echo "  CheersAI-Nexus 部署脚本"
    echo "========================================"
    echo ""
    
    find_package
    show_summary
    
    # 确认部署
    if [ "${DRY_RUN}" = false ]; then
        echo ""
        read -p "确认执行部署? (y/n): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "部署已取消"
            exit 0
        fi
    fi
    
    backup_current
    upload_package
    restart_services
    verify_deployment
    
    log_success "部署完成!"
}

main "$@"
