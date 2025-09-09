#!/bin/bash

# Spring Boot生产环境部署脚本

set -e

echo "🚀 开始部署Spring Boot应用到生产环境..."

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down

# 清理旧镜像（可选）
echo "🧹 清理旧镜像..."
docker system prune -f

# 构建并启动服务
echo "🔨 构建并启动服务..."
docker-compose up -d --build

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 健康检查
echo "💊 执行健康检查..."
if curl -f http://localhost/api/v1/public/health; then
    echo "✅ 应用部署成功！"
    echo "🌐 应用访问地址: http://localhost"
    echo "📊 健康检查: http://localhost/api/v1/public/health"
    echo "📋 应用信息: http://localhost/api/v1/public/info"
else
    echo "❌ 健康检查失败，请检查日志"
    docker-compose logs app
    exit 1
fi

echo "🎉 部署完成！" 