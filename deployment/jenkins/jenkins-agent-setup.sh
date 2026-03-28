#!/bin/bash
#==============================================================================
# Jenkins Agent 启动脚本 (在部署服务器上运行)
# 
# 此脚本用于配置 Jenkins Agent 连接到 Jenkins Master
# 
# 使用方法:
#   1. 在 Jenkins Master 上创建 Agent 节点
#   2. 在 Agent 机器上运行此脚本
#==============================================================================

set -e

# 配置 - 根据您的环境修改
JENKINS_URL="http://175.178.236.183:8080"  # Jenkins Master 地址
AGENT_NAME="nexus-deploy-agent"
AGENT_HOME="/home/jenkins/agent"
AGENT_USER="jenkins"

# SSH 密钥配置
SSH_KEY_PATH="${AGENT_HOME}/.ssh/id_rsa"

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
  --install      安装并配置 Jenkins Agent
  --start        启动 Agent
  --stop         停止 Agent
  --restart      重启 Agent
  --status       查看 Agent 状态
  -h, --help     显示帮助

示例:
  $0 --install   # 首次安装
  $0 --start     # 启动 Agent
EOF
    exit 1
}

# 安装依赖
install_deps() {
    log_info "安装依赖..."
    
    if command -v yum &> /dev/null; then
        sudo yum install -y java-21-openjdk git curl wget
    elif command -v apt &> /dev/null; then
        sudo apt update && sudo apt install -y openjdk-21-jdk git curl wget
    fi
    
    # 验证 Java
    if java -version 2>&1 | head -1 | grep -q "21"; then
        log_success "Java 21 已安装"
    else
        log_warn "建议安装 Java 21"
    fi
}

# 创建用户和目录
create_user_dirs() {
    log_info "创建用户和目录..."
    
    # 创建 jenkins 用户（如果不存在）
    if ! id "${AGENT_USER}" &>/dev/null; then
        sudo useradd -m -s /bin/bash ${AGENT_USER}
        log_success "用户 ${AGENT_USER} 已创建"
    fi
    
    # 创建 Agent 目录
    sudo mkdir -p ${AGENT_HOME}
    sudo chown -R ${AGENT_USER}:${AGENT_USER} ${AGENT_HOME}
    
    # 创建 SSH 目录
    sudo mkdir -p ${AGENT_HOME}/.ssh
    sudo chown -R ${AGENT_USER}:${AGENT_USER} ${AGENT_HOME}/.ssh
    sudo chmod 700 ${AGENT_HOME}/.ssh
}

# 配置 SSH 密钥
setup_ssh_key() {
    log_info "配置 SSH 密钥..."
    
    # 生成 SSH 密钥（如果不存在）
    if [ ! -f "${SSH_KEY_PATH}" ]; then
        sudo -u ${AGENT_USER} ssh-keygen -t rsa -b 4096 -N "" -f ${SSH_KEY_PATH}
        log_success "SSH 密钥已生成"
    fi
    
    # 显示公钥（需要添加到 Jenkins Master）
    echo ""
    log_info "请将以下公钥添加到 Jenkins Master:"
    echo ""
    sudo cat ${SSH_KEY_PATH}.pub
    echo ""
    
    read -p "按 Enter 继续..."
}

# 下载 agent.jar
download_agent() {
    log_info "下载 Jenkins Agent JAR..."
    
    local agent_jar="${AGENT_HOME}/agent.jar"
    
    if [ ! -f "${agent_jar}" ]; then
        sudo -u ${AGENT_USER} wget -q -O ${agent_jar} \
            "${JENKINS_URL}/jnlpJars/agent.jar"
        log_success "Agent JAR 已下载"
    fi
}

# 创建 systemd 服务
create_service() {
    log_info "创建 systemd 服务..."
    
    cat > /tmp/jenkins-agent.service << EOF
[Unit]
Description=Jenkins Agent
After=network.target

[Service]
User=${AGENT_USER}
Group=${AGENT_USER}
WorkingDirectory=${AGENT_HOME}

ExecStart=/usr/bin/java -jar ${AGENT_HOME}/agent.jar \
    -url ${JENKINS_URL}/ \
    -name ${AGENT_NAME} \
    -secret \$(cat ${AGENT_HOME}/secret) \
    -workDir ${AGENT_HOME}

Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
    
    sudo mv /tmp/jenkins-agent.service /etc/systemd/system/
    sudo systemctl daemon-reload
    log_success "systemd 服务已创建"
}

# 获取 secret
get_secret() {
    echo ""
    log_info "获取 Agent Secret..."
    echo ""
    log_info "请在 Jenkins Master 控制台执行以下命令获取 secret:"
    echo ""
    echo " Manage Jenkins → Nodes → ${AGENT_NAME} → Configure"
    echo "  或访问: ${JENKINS_URL}/computer/${AGENT_NAME}/configure"
    echo ""
    read -p "请输入 Agent Secret: " AGENT_SECRET
    
    if [ -z "${AGENT_SECRET}" ]; then
        log_error "Secret 不能为空"
        exit 1
    fi
    
    echo ${AGENT_SECRET} | sudo tee ${AGENT_HOME}/secret > /dev/null
    sudo chown ${AGENT_USER}:${AGENT_USER} ${AGENT_HOME}/secret
    sudo chmod 600 ${AGENT_HOME}/secret
}

# 启动服务
start_agent() {
    log_info "启动 Jenkins Agent..."
    sudo systemctl start jenkins-agent
    sudo systemctl enable jenkins-agent
    log_success "Agent 已启动"
}

# 停止服务
stop_agent() {
    log_info "停止 Jenkins Agent..."
    sudo systemctl stop jenkins-agent || true
    log_success "Agent 已停止"
}

# 查看状态
status_agent() {
    sudo systemctl status jenkins-agent --no-pager -l
}

# 主函数
main() {
    local action=""
    
    for arg in "$@"; do
        case $arg in
            --install)
                action="install"
                ;;
            --start)
                action="start"
                ;;
            --stop)
                action="stop"
                ;;
            --restart)
                action="restart"
                ;;
            --status)
                action="status"
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
        usage
    fi
    
    echo ""
    echo "========================================"
    echo "  Jenkins Agent 配置脚本"
    echo "========================================"
    echo ""
    
    case $action in
        install)
            install_deps
            create_user_dirs
            setup_ssh_key
            download_agent
            get_secret
            create_service
            start_agent
            log_success "安装完成!"
            ;;
        start)
            start_agent
            ;;
        stop)
            stop_agent
            ;;
        restart)
            stop_agent
            start_agent
            ;;
        status)
            status_agent
            ;;
    esac
}

main "$@"
