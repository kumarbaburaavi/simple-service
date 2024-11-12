FROM amazoncorretto:21-alpine

RUN apk update && apk add curl && apk cache clean
RUN /usr/sbin/adduser -D spring && /usr/sbin/addgroup spring spring
RUN chown -R spring:spring /tmp
USER spring:spring
ARG JAR_FILE=target/*.jar
ARG TAG_VERSION
COPY ${JAR_FILE} app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal"
ENV APPLICATION_VERSION=$TAG_VERSION
ENTRYPOINT ["java","-jar","/app.jar"]
