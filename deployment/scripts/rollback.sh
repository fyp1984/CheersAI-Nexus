#!/bin/bash
#==============================================================================
# CheersAI-Nexus 回滚脚本
# 回滚到上一个备份版本
# 
# 使用方法: ./rollback.sh [备份版本]
# 示例: 
#   ./rollback.sh                           # 回滚到上一个备份
#   ./rollback.sh backup-20260326_120000    # 回滚到指定版本
#==============================================================================

set -e
set -u

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEFAULT_APP_DIR="/home/nexus/app"
DEFAULT_BACKUP_DIR="/home/nexus/backup"
SERVER_HOST="175.178.236.183"
SERVER_USER="nexus"

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 显示帮助
usage() {
    cat << EOF
使用方法: $0 [选项]

选项:
  --backup=<版本>      指定要回滚的备份版本（默认为上一个备份）
  --server=<主机>      目标服务器地址 (默认: ${SERVER_HOST})
  --user=<用户>        SSH 用户 (默认: ${SERVER_USER})
  --list               列出所有可用备份
  -h, --help           显示帮助信息

示例:
  $0                    # 回滚到上一个备份
  $0 --list            # 列出所有备份
  $0 --backup=backup-20260326_120000  # 回滚到指定版本
EOF
    exit 1
}

# 列出可用备份
list_backups() {
    log_info "可用备份列表:"
    ssh ${SERVER_USER}@${SERVER_HOST} "ls -lt ${DEFAULT_BACKUP_DIR}/backup-* 2>/dev/null || echo '没有可用备份'"
}

# 执行回滚
do_rollback() {
    local backup_name=$1
    
    log_info "========== 开始回滚 =========="
    log_info "备份版本: ${backup_name}"
    
    # 检查备份是否存在
    if ! ssh ${SERVER_USER}@${SERVER_HOST} "[ -d ${DEFAULT_BACKUP_DIR}/${backup_name} ]"; then
        log_error "备份不存在: ${backup_name}"
        exit 1
    fi
    
    # 确认回滚
    echo ""
    log_warn "警告: 此操作将回滚到以下版本:"
    echo "  备份: ${backup_name}"
    echo "  备份时间: $(ssh ${SERVER_USER}@${SERVER_HOST} "stat -c '%y' ${DEFAULT_BACKUP_DIR}/${backup_name}" 2>/dev/null | cut -d'.' -f1)"
    echo ""
    read -p "确认回滚? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "回滚已取消"
        exit 0
    fi
    
    # 停止服务
    log_info "停止服务..."
    ssh ${SERVER_USER}@${SERVER_HOST} "
        systemctl stop nexus-auditlog 2>/dev/null || true
        systemctl stop nexus-membership 2>/dev/null || true
        systemctl stop nexus-product 2>/dev/null || true
        systemctl stop nexus-feedback 2>/dev/null || true
        systemctl stop nexus-user-management 2>/dev/null || true
        systemctl stop nexus-auth 2>/dev/null || true
    "
    
    # 回滚文件
    log_info "恢复文件..."
    ssh ${SERVER_USER}@${SERVER_HOST} "
        set -e
        
        cd ${DEFAULT_APP_DIR}
        
        # 备份当前版本（以防万一）
        if [ -d 'backend' ] || [ -d 'frontend' ]; then
            mkdir -p ${DEFAULT_BACKUP_DIR}/backup-rollback-$(date +%Y%m%d_%H%M%S)
            cp -r backend ${DEFAULT_BACKUP_DIR}/backup-rollback-$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || true
            cp -r frontend ${DEFAULT_BACKUP_DIR}/backup-rollback-$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || true
        fi
        
        # 恢复备份
        rm -rf backend frontend config
        cp -r ${DEFAULT_BACKUP_DIR}/${backup_name}/backend . 2>/dev/null || true
        cp -r ${DEFAULT_BACKUP_DIR}/${backup_name}/frontend . 2>/dev/null || true
        cp -r ${DEFAULT_BACKUP_DIR}/${backup_name}/config . 2>/dev/null || true
        
        echo '文件恢复完成'
    "
    
    # 重启服务
    log_info "重启服务..."
    ssh ${SERVER_USER}@${SERVER_HOST} "
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
        nginx -t && nginx -s reload || nginx
    "
    
    log_success "回滚完成"
    log_info "服务已重启，请检查运行状态"
}

# 获取最新备份
get_latest_backup() {
    ssh ${SERVER_USER}@${SERVER_HOST} "ls -t ${DEFAULT_BACKUP_DIR}/backup-* 2>/dev/null | head -1" | xargs basename
}

# 主函数
main() {
    local backup_to_restore=""
    
    for arg in "$@"; do
        case $arg in
            --backup=*)
                backup_to_restore="${arg#*=}"
                ;;
            --server=*)
                SERVER_HOST="${arg#*=}"
                ;;
            --user=*)
                SERVER_USER="${arg#*=}"
                ;;
            --list)
                list_backups
                exit 0
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
    echo "  CheersAI-Nexus 回滚脚本"
    echo "========================================"
    echo ""
    
    if [ -z "${backup_to_restore}" ]; then
        backup_to_restore=$(get_latest_backup)
        if [ -z "${backup_to_restore}" ]; then
            log_error "没有找到可用的备份"
            list_backups
            exit 1
        fi
        log_info "将回滚到上一个备份: ${backup_to_restore}"
    fi
    
    do_rollback "${backup_to_restore}"
}

main "$@"
