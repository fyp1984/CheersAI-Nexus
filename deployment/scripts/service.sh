#!/bin/bash
#==============================================================================
# 服务管理脚本
# 统一管理所有 CheersAI-Nexus 微服务
# 
# 使用方法: ./service.sh <命令> [服务名]
# 示例: 
#   ./service.sh start        # 启动所有服务
#   ./service.sh stop         # 停止所有服务
#   ./service.sh restart      # 重启所有服务
#   ./service.sh status       # 查看所有服务状态
#   ./service.sh start auth   # 仅启动 auth 服务
#   ./service.sh logs auth    # 查看 auth 服务日志
#==============================================================================

set -e

# 配置
SERVER_USER="nexus"
SERVER_HOST="175.178.236.183"
SERVICES=("nexus-auth" "nexus-user-management" "nexus-feedback" "nexus-product")

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 显示帮助
usage() {
    cat << EOF
使用方法: $0 <命令> [服务名]

命令:
  start     启动服务（可指定单个服务）
  stop      停止服务（可指定单个服务）
  restart   重启服务（可指定单个服务）
  status    查看服务状态
  logs      查看服务日志（按 Ctrl+C 退出）
  enable    设置开机自启
  disable   取消开机自启

服务名:
  auth               认证服务 (端口 8082)
  user-management    用户管理服务 (端口 8083)
  feedback           反馈服务 (端口 8084)
  product            产品服务 (端口 8085)
  all                所有服务（默认）

示例:
  $0 start              # 启动所有服务
  $0 stop auth          # 停止 auth 服务
  $0 restart all        # 重启所有服务
  $0 status             # 查看所有服务状态
  $0 logs user-management  # 查看用户管理服务日志
  $0 enable             # 设置所有服务开机自启
EOF
    exit 1
}

# 执行远程命令
remote_exec() {
    ssh ${SERVER_USER}@${SERVER_HOST} "$1"
}

# 启动服务
start_service() {
    local service=$1
    log_info "启动 ${service}..."
    remote_exec "sudo systemctl start ${service}"
}

# 停止服务
stop_service() {
    local service=$1
    log_info "停止 ${service}..."
    remote_exec "sudo systemctl stop ${service}"
}

# 重启服务
restart_service() {
    local service=$1
    log_info "重启 ${service}..."
    remote_exec "sudo systemctl restart ${service}"
}

# 查看状态
status_service() {
    local service=$1
    remote_exec "systemctl status ${service} --no-pager -l" || true
}

# 查看日志
logs_service() {
    local service=$1
    log_info "查看 ${service} 日志 (按 Ctrl+C 退出)..."
    ssh -t ${SERVER_USER}@${SERVER_HOST} "sudo journalctl -u ${service} -f --no-pager"
}

# 设置开机自启
enable_service() {
    local service=$1
    log_info "设置 ${service} 开机自启..."
    remote_exec "sudo systemctl enable ${service}"
}

# 取消开机自启
disable_service() {
    local service=$1
    log_info "取消 ${service} 开机自启..."
    remote_exec "sudo systemctl disable ${service}"
}

# 主函数
main() {
    if [ $# -lt 1 ]; then
        usage
    fi
    
    local command=$1
    local target=${2:-all}
    
    echo ""
    echo "========================================"
    echo "  CheersAI-Nexus 服务管理"
    echo "  命令: ${command}, 目标: ${target}"
    echo "========================================"
    echo ""
    
    case $command in
        start)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    start_service "$svc"
                done
            else
                start_service "nexus-${target}"
            fi
            log_success "启动完成"
            ;;
        stop)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    stop_service "$svc"
                done
            else
                stop_service "nexus-${target}"
            fi
            log_success "停止完成"
            ;;
        restart)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    restart_service "$svc"
                done
            else
                restart_service "nexus-${target}"
            fi
            log_success "重启完成"
            ;;
        status)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    echo ""
                    echo "=== ${svc} ==="
                    status_service "$svc"
                done
            else
                status_service "nexus-${target}"
            fi
            ;;
        logs)
            if [ "$target" = "all" ]; then
                log_warn "请指定要查看日志的服务"
                usage
            else
                logs_service "nexus-${target}"
            fi
            ;;
        enable)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    enable_service "$svc"
                done
            else
                enable_service "nexus-${target}"
            fi
            log_success "设置完成"
            ;;
        disable)
            if [ "$target" = "all" ]; then
                for svc in "${SERVICES[@]}"; do
                    disable_service "$svc"
                done
            else
                disable_service "nexus-${target}"
            fi
            log_success "设置完成"
            ;;
        *)
            log_error "未知命令: ${command}"
            usage
            ;;
    esac
}

main "$@"
