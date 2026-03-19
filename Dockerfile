# Giai đoạn 1: Build ứng dụng
FROM maven:3.9.9-eclipse-temurin-23 AS builder
WORKDIR /app
COPY . .
# Biên dịch code thành file .jar (bỏ qua chạy thử test để build nhanh)
RUN mvn package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
FROM eclipse-temurin:23-jre
WORKDIR /app
# Chỉ lấy file .jar từ builder (GĐ1)
COPY --from=builder /app/target/*.jar app.jar
# Mở cổng 3000 theo cấu hình trong application.properties
EXPOSE 3000
# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
