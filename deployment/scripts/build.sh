#!/bin/bash
#==============================================================================
# CheersAI-Nexus 构建脚本
# 用于构建前端和后端单体打包
# 
# 使用方法: ./build.sh [环境]
# 示例: ./build.sh uat
#==============================================================================

set -e  # 遇到错误立即退出
set -u  # 使用未定义变量时报错

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEPLOY_DIR="${PROJECT_ROOT}/deployment"
BACKEND_DIR="${PROJECT_ROOT}/nexus-backend"
FRONTEND_DIR="${PROJECT_ROOT}/nexus-frontend"
OUTPUT_DIR="${DEPLOY_DIR}/output"
BUILD_TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BUILD_VERSION=${1:-uat}

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示使用方法
usage() {
    echo "使用方法: $0 [环境]"
    echo "  环境: uat (默认), prod"
    exit 1
}

# 检查必要工具
check_dependencies() {
    log_info "检查构建依赖..."
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装，请先安装 JDK 21"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    log_info "Java 版本: ${JAVA_VERSION}"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装，请先安装 Maven"
        exit 1
    fi
    MVN_VERSION=$(mvn -version 2>&1 | head -1)
    log_info "Maven 版本: ${MVN_VERSION}"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js 未安装，请先安装 Node.js"
        exit 1
    fi
    NODE_VERSION=$(node -v)
    log_info "Node.js 版本: ${NODE_VERSION}"
    
    # 检查 npm
    if ! command -v npm &> /dev/null; then
        log_error "npm 未安装"
        exit 1
    fi
    NPM_VERSION=$(npm -v)
    log_info "npm 版本: ${NPM_VERSION}"
    
    log_success "所有依赖检查通过"
}

# 清理旧构建
clean_build() {
    log_info "清理旧构建文件..."
    rm -rf "${OUTPUT_DIR}"
    mkdir -p "${OUTPUT_DIR}/backend"
    mkdir -p "${OUTPUT_DIR}/frontend"
    mkdir -p "${OUTPUT_DIR}/config"
    mkdir -p "${OUTPUT_DIR}/systemd"
    log_success "构建目录已创建: ${OUTPUT_DIR}"
}

# 构建后端
build_backend() {
    log_info "========== 开始构建后端 =========="
    
    cd "${BACKEND_DIR}"
    
    # 构建 common 模块
    log_info "构建 common 模块..."
    mvn -pl common -am clean install -DskipTests -q
    
    # 构建所有业务模块
    log_info "构建业务模块 (auth, user-management, feedback, product, membership, auditlog)..."
    mvn clean package -DskipTests -q
    
    # 复制 JAR 文件到输出目录
    log_info "复制 JAR 文件到输出目录..."
    
    # Auth
    if [ -f "auth/target/auth-0.0.1-SNAPSHOT.jar" ]; then
        cp "auth/target/auth-0.0.1-SNAPSHOT.jar" "${OUTPUT_DIR}/backend/nexus-auth.jar"
        log_info "✓ auth JAR 已复制"
    fi
    
    # User Management
    if [ -f "user-management/target/user-management-0.0.1-SNAPSHOT.jar" ]; then
        cp "user-management/target/user-management-0.0.1-SNAPSHOT.jar" "${OUTPUT_DIR}/backend/nexus-user-management.jar"
        log_info "✓ user-management JAR 已复制"
    fi
    
    # Feedback
    if [ -f "feedback/target/feedback-1.0.0.jar" ]; then
        cp "feedback/target/feedback-1.0.0.jar" "${OUTPUT_DIR}/backend/nexus-feedback.jar"
        log_info "✓ feedback JAR 已复制"
    fi
    
    # Product
    if [ -f "product/target/product-1.0.0.jar" ]; then
        cp "product/target/product-1.0.0.jar" "${OUTPUT_DIR}/backend/nexus-product.jar"
        log_info "✓ product JAR 已复制"
    fi

    # Membership
    if [ -f "membership/target/membership-1.0.0.jar" ]; then
        cp "membership/target/membership-1.0.0.jar" "${OUTPUT_DIR}/backend/nexus-membership.jar"
        log_info "✓ membership JAR 已复制"
    fi

    # AuditLog
    if [ -f "auditlog/target/auditlog-0.0.1-SNAPSHOT.jar" ]; then
        cp "auditlog/target/auditlog-0.0.1-SNAPSHOT.jar" "${OUTPUT_DIR}/backend/nexus-auditlog.jar"
        log_info "✓ auditlog JAR 已复制"
    fi
    
    log_success "后端构建完成"
}

# 构建前端
build_frontend() {
    log_info "========== 开始构建前端 =========="
    
    cd "${FRONTEND_DIR}"
    
    # 安装依赖
    log_info "安装前端依赖..."
    npm install 2>&1 | tail -5
    
    # 构建生产版本
    log_info "构建前端生产版本..."
    npm run build
    
    # 复制构建产物
    log_info "复制前端构建产物..."
    cp -r "${FRONTEND_DIR}/dist" "${OUTPUT_DIR}/frontend/"
    
    log_success "前端构建完成"
}

# 准备部署配置
prepare_deploy_configs() {
    log_info "========== 准备部署配置 =========="

    # 配置目录（application-prod.yml, .env.*）
    cp -r "${DEPLOY_DIR}/config/." "${OUTPUT_DIR}/config/"
    # systemd 服务文件
    cp -r "${DEPLOY_DIR}/systemd/." "${OUTPUT_DIR}/systemd/"

    log_success "部署配置准备完成"
}

# 创建部署包
create_deploy_package() {
    log_info "========== 创建部署包 =========="
    
    PACKAGE_NAME="nexus-${BUILD_VERSION}-${BUILD_TIMESTAMP}.tar.gz"
    PACKAGE_PATH="${DEPLOY_DIR}/${PACKAGE_NAME}"
    
    # 创建部署清单
    cat > "${OUTPUT_DIR}/deploy-manifest.txt" << EOF
构建时间: ${BUILD_TIMESTAMP}
构建版本: ${BUILD_VERSION}
构建环境: ${BUILD_VERSION}
构建主机: $(hostname)
构建用户: $(whoami)

服务列表:
- nexus-auth.jar      (端口: 8082, API: /api/v1/auth)
- nexus-user-management.jar  (端口: 8083, API: /api/v1/users)
- nexus-feedback.jar  (端口: 8084, API: /api/v1/feedbacks)
- nexus-product.jar    (端口: 8085, API: /api/v1/products)
- nexus-membership.jar (端口: 8086, API: /api/v1/plans,/api/v1/subscriptions)
- nexus-auditlog.jar   (端口: 8087, API: /api/v1/audit-logs)

前端静态资源: dist/
部署配置: config/, systemd/
EOF
    
    # 创建部署包
    cd "${OUTPUT_DIR}"
    tar -czf "${PACKAGE_PATH}" backend/ frontend/ config/ systemd/ deploy-manifest.txt
    
    # 生成 MD5 校验文件
    md5sum "${PACKAGE_NAME}" > "${PACKAGE_PATH}.md5"
    
    # 创建 latest symbolic link
    ln -sf "${PACKAGE_NAME}" "${DEPLOY_DIR}/nexus-${BUILD_VERSION}-latest.tar.gz"
    ln -sf "${PACKAGE_NAME}.md5" "${DEPLOY_DIR}/nexus-${BUILD_VERSION}-latest.tar.gz.md5"
    
    log_success "部署包已创建: ${PACKAGE_PATH}"
    log_info "包大小: $(du -h "${PACKAGE_PATH}" | cut -f1)"
    log_info "MD5: $(cat "${PACKAGE_PATH}.md5")"
}

# 显示构建结果
show_summary() {
    log_info "========== 构建完成 =========="
    echo ""
    echo "输出目录: ${OUTPUT_DIR}"
    echo "部署包:   ${DEPLOY_DIR}/nexus-${BUILD_VERSION}-latest.tar.gz"
    echo ""
    echo "后端 JAR 文件:"
    ls -lh "${OUTPUT_DIR}/backend/"*.jar 2>/dev/null | awk '{print "  - " $9 " (" $5 ")"}'
    echo ""
    echo "前端构建:"
    echo "  - ${OUTPUT_DIR}/frontend/dist/"
    echo ""
}

# 主函数
main() {
    echo ""
    echo "========================================"
    echo "  CheersAI-Nexus 构建脚本"
    echo "  版本: ${BUILD_VERSION}"
    echo "  时间: ${BUILD_TIMESTAMP}"
    echo "========================================"
    echo ""
    
    # 检查依赖
    check_dependencies
    
    # 清理并创建目录
    clean_build
    
    # 构建后端
    build_backend
    
    # 构建前端
    build_frontend

    # 准备部署配置
    prepare_deploy_configs
    
    # 创建部署包
    create_deploy_package
    
    # 显示结果
    show_summary
    
    log_success "构建任务完成!"
}

# 执行主函数
main "$@"
