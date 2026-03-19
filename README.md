# Online Railway Reservation System

A comprehensive, scalable microservices-based backend system for booking railway tickets. Built using Java, Spring Boot, and Spring Cloud, it demonstrates advanced architectural patterns including API Gateway routing, dynamic service discovery, stateless authentication, synchronous inter-service communication, and third-party payment integration.

## 🏗 System Architecture

The project is broken down into independent microservices, each handling a specific domain:

- **API Gateway (`api-gateway`)**: Centralized entry point, routes and filters incoming requests based on configurations.
- **Service Registry (`service-registry`)**: Netflix Eureka server for dynamic service registration and discovery.
- **User Service (`user-service`)**: Handles user registration, login, and profile management. Issues JSON Web Tokens (JWT) for stateless authentication.
- **Train Service (`train-service`)**: Manages train schedules, stations, and seat availability.
- **Reservation Service (`reservation_service`)**: Handles the core logic of booking tickets and passenger management. Utilizes Spring Cloud OpenFeign to synchronize with the Payment Service.
- **Payment Service (`payment_service`)**: Integrates with the Stripe API to handle secure ticket purchasing and processes asynchronous Stripe Webhooks to capture successful payments.

## 🚀 Technologies Used

- **Core**: Java 17, Spring Boot 3.5.x, Spring Cloud 2025
- **Database**: MySQL (containerized)
- **Security**: Spring Security, JSON Web Tokens (JWT)
- **Inter-service Communication**: Spring Cloud OpenFeign
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Infrastructure**: Docker Compose, Stripe CLI

## ⚙️ How to Run Locally

### 1. Prerequisites
- Java 17+ installed
- Maven installed (`mvn`)
- Docker & Docker Compose
- Stripe CLI

### 2. Start the Database
The project uses a containerized MySQL instance. Navigate to the root directory and run:
```bash
docker-compose up -d
```
*Creates a MySQL 8 container running on port 3306 with root user `root` and password `0000`.*

### 3. Setup Stripe Webhooks
The `payment_service` relies on Stripe for processing payments.
1. Sign up for a free [Stripe Developer Account](https://stripe.com).
2. Get your Secret API Key and configure it in `payment_service/src/main/resources/application.properties` (`stripe.api.key=...`).
3. Forward Stripe events to your local webhook endpoint:
```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```
4. Copy the logged **webhook signing secret** and place it in the application properties as `stripe.webhook.secret=...`.

### 4. Run the Microservices
Start the services in the following order (either via your IDE or `mvn spring-boot:run` in each directory):

1. **Service Registry** (Eureka Server) -> Runs on `http://localhost:8761`
2. **API Gateway** -> Runs on `http://localhost:8080`
3. **User Service** -> Runs on `http://localhost:8081`
4. **Train Service** -> Runs on `http://localhost:8082`
5. **Reservation Service** -> Runs on `http://localhost:8083`
6. **Payment Service** -> Runs on `http://localhost:8084`

All outside communication should be routed through the **API Gateway** (`http://localhost:8080`).

## 📚 API Documentation

Every microservice includes Auto-generated Swagger documentation. Once the API Gateway and services are running, you can access the Swagger UIs (example):
* `http://localhost:8081/swagger-ui.html` (User Service APIs)
* `http://localhost:8082/swagger-ui.html` (Train Service APIs)
* `http://localhost:8083/swagger-ui.html` (Reservation Service APIs)
* `http://localhost:8084/swagger-ui.html` (Payment Service APIs)

*(Note: Postman collections are also included in the root directory: `railway.postman_collection.json`)*
