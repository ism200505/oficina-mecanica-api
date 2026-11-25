# ---------- Etapa 1: compila e roda os testes ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package

# ---------- Etapa 2: imagem final enxuta, sem o código-fonte ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home oficina
COPY --from=build /app/target/oficina-mecanica-api.jar app.jar
USER oficina
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
