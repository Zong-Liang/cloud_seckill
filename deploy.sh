#!/bin/bash
# ========================================
# Cloud Seckill 生产环境部署脚本
# 用法: ./deploy.sh [start|stop|restart|build|logs]
# ========================================

set -e

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_NAME="seckill"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 和 Docker Compose
check_dependencies() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    log_info "依赖检查通过"
}

# 构建项目
build() {
    log_info "开始构建项目..."
    
    # Maven 构建
    mvn clean package -DskipTests -q
    
    if [ $? -eq 0 ]; then
        log_info "Maven 构建成功"
    else
        log_error "Maven 构建失败"
        exit 1
    fi

    # Docker 镜像构建
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME build
    
    log_info "Docker 镜像构建完成"
}

# 启动服务
start() {
    log_info "启动服务..."
    
    # 启动中间件
    log_info "启动中间件..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d mysql redis nacos
    
    log_info "等待中间件启动完成 (30秒)..."
    sleep 30
    
    # 启动 RocketMQ
    log_info "启动 RocketMQ..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d rocketmq-namesrv rocketmq-broker
    sleep 10
    
    # 启动监控组件
    log_info "启动监控组件..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d prometheus grafana
    
    # 启动应用服务
    log_info "启动应用服务..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d seckill-gateway seckill-stock seckill-order
    
    log_info "所有服务启动完成"
    show_status
}

# 停止服务
stop() {
    log_info "停止服务..."
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    log_info "服务已停止"
}

# 重启服务
restart() {
    stop
    sleep 5
    start
}

# 查看日志
logs() {
    local service=${1:-}
    if [ -z "$service" ]; then
        docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f --tail=100
    else
        docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f --tail=100 $service
    fi
}

# 显示服务状态
show_status() {
    echo ""
    log_info "服务状态:"
    docker compose -f $COMPOSE_FILE -p $PROJECT_NAME ps
    
    echo ""
    log_info "访问地址:"
    echo "  - 网关入口: http://localhost:9000"
    echo "  - 库存服务: http://localhost:9002/doc.html"
    echo "  - 订单服务: http://localhost:9001/doc.html"
    echo "  - Nacos: http://localhost:8848/nacos (nacos/nacos)"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3000 (admin/admin123)"
}

# 帮助信息
help() {
    echo "Cloud Seckill 部署脚本"
    echo ""
    echo "用法: $0 [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  build     构建项目和 Docker 镜像"
    echo "  start     启动所有服务"
    echo "  stop      停止所有服务"
    echo "  restart   重启所有服务"
    echo "  logs      查看日志 (可指定服务名)"
    echo "  status    查看服务状态"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 build          # 构建项目"
    echo "  $0 start          # 启动服务"
    echo "  $0 logs seckill-stock  # 查看库存服务日志"
}

# 主函数
main() {
    check_dependencies

    case "${1:-}" in
        build)
            build
            ;;
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        logs)
            logs $2
            ;;
        status)
            show_status
            ;;
        help|--help|-h)
            help
            ;;
        *)
            help
            exit 1
            ;;
    esac
}

main "$@"
