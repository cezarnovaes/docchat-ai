# ===========================
# Stage 1: Build
# ===========================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia arquivos de dependencia
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Baixa dependencias (cacheia)
RUN mvn dependency:go-offline -B

# Copia codigo fonte
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests

# ===========================
# Stage 2: Runtime
# ===========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR do stage de build
COPY --from=build /app/target/*.jar app.jar

# Expoe a porta
EXPOSE 8080

# Comando para executar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
