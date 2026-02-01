# ========================================
# seckill-gateway 网关服务 Dockerfile
# 多阶段构建，优化镜像大小
# ========================================

# 阶段1: 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存机制
COPY pom.xml .
COPY seckill_common/pom.xml seckill_common/
COPY seckill_gateway/pom.xml seckill_gateway/
COPY seckill_order/pom.xml seckill_order/
COPY seckill_stock/pom.xml seckill_stock/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY seckill_gateway/src seckill_gateway/src

# 构建项目
RUN mvn clean package -DskipTests -pl seckill_gateway

# 阶段2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="seckill@example.com"
LABEL version="3.0.0"
LABEL description="Seckill Gateway Service - API 网关"

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户运行应用
RUN addgroup -S seckill && adduser -S seckill -G seckill
USER seckill

WORKDIR /app

# 复制构建产物
COPY --from=builder /build/seckill_gateway/target/*.jar app.jar

# JVM 参数（网关需要更多内存处理并发）
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=50 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:9000/actuator/health || exit 1

# 暴露端口
EXPOSE 9000

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
