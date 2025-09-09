# 使用官方OpenJDK运行时作为父镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven包装器和pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 下载依赖（利用Docker层缓存）
RUN ./mvnw dependency:go-offline

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -Dspring.profiles.active=prod"

# 运行jar文件
CMD ["java", "-jar", "/app/target/myproject-0.0.1-SNAPSHOT.jar"] 