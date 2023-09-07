FROM swr.cn-south-1.myhuaweicloud.com/hycan-test/java8:hycan

MAINTAINER xianweijian

COPY target/*.jar /app.jar

# 调整时区
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

# jvm基础变量
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ENV PROFILE_ACTIVE="dev"
ENTRYPOINT java ${JAVA_OPTS} -jar /app.jar --spring.profile.active=${PROFILE_ACTIVE}