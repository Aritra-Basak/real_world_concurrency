# Concurrent User Handling POC – Spring Boot

This repository contains a **Spring Boot–based Proof of Concept (POC)** that demonstrates how a real-world backend system handles **concurrent users**, **simultaneous updates**, **fault tolerance**, **rate limiting**, **caching**, and **monitoring**.

The POC is intentionally designed to be **simple, realistic, and well-documented**, focusing on practical backend engineering challenges seen in production systems.

📘 **Each class and configuration file contains in-code explanations** describing *why* specific mechanisms are used.

---

## 🚀 What This POC Demonstrates

✅ Concurrent access to the same API by multiple users  
✅ Concurrent modification of the same database record  
✅ Data consistency using **Optimistic Locking**  
✅ Fault tolerance using **Resilience4j**  
✅ API protection using **Rate Limiting & Bulkhead**  
✅ Performance optimization using **Caffeine Cache**  
✅ In-memory persistence using **H2 Database**  
✅ Monitoring via **Spring Boot Actuator & Micrometer**  
✅ Global exception handling  
✅ Multithreaded concurrency testing with **@SpringBootTest**

---

## 🛠 Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- H2 In-Memory Database
- Spring Cache Abstraction
- Caffeine Cache
- Resilience4j
  - Circuit Breaker
  - Rate Limiter
  - Bulkhead
  - Retry
- Spring Boot Actuator
- Micrometer
- JUnit 5

---

## 📦 Key Dependencies

```xml
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-cache
spring-boot-starter-actuator
resilience4j-spring-boot3
caffeine
h2
spring-boot-starter-test
```

All dependencies are defined in `pom.xml`.

---

## 🗂 Project Structure

```
src
 ├── main
 │   └── java
 │       └── com.example.concurrentpoc
 │            ├── controller        → REST APIs
 │            ├── facade            → Resilience4j layer
 │            ├── service           → Business logic
 │            ├── entity            → JPA entities
 │            ├── repository        → Data access
 │            ├── config            → Cache & resilience config
 │            └── exception         → Custom & global exceptions
 │
 └── test
     └── java
         └── com.example.concurrentpoc
              └── UserConcurrencyTest.java
```

✅ All important classes include inline comments explaining their purpose.

---

## 🧠 Core Mechanisms Used

### 1️⃣ Optimistic Locking (Concurrency Control)

Implemented using JPA’s `@Version` annotation.

```java
@Version
private Long version;
```

- Prevents lost updates when multiple users try to update the same record.
- Only **one update succeeds**, others fail with a conflict.
- Ensures database consistency without locking rows.

---

### 2️⃣ Caching with Caffeine

```java
@Cacheable(cacheNames = "users", key = "#id")
```

- Read-through cache for user data.
- Reduces DB load during high read traffic.
- Cache is updated/evicted on write operations.

---

### 3️⃣ Fault Tolerance with Resilience4j

Implemented at the **Facade layer**:

```java
@CircuitBreaker
@RateLimiter
@Bulkhead
@Retry
```

| Pattern | Purpose |
|-------|--------|
| Circuit Breaker | Prevent cascading failures |
| Rate Limiter | Protect APIs from overload |
| Bulkhead | Limit concurrent executions |
| Retry | Handle transient failures |

---

### 4️⃣ Rate Limiting

- Separate limits for read and write operations.
- Prevents abuse and ensures system stability.

```yaml
limit-for-period: 5
limit-refresh-period: 1s
```

---

### 5️⃣ Global Exception Handling

Implemented using `@RestControllerAdvice`.

Handles:
- 404 – Resource Not Found
- 409 – Concurrent Update Conflict
- 429 – Rate Limit Exceeded
- 500 – Internal Server Error

Produces consistent API error responses.

---

### 6️⃣ Monitoring & Observability

Provided using **Spring Boot Actuator** and **Micrometer**.

Available endpoints:

```
/actuator/health
/actuator/metrics
/actuator/metrics/http.server.requests
/actuator/metrics/resilience4j.circuitbreaker.calls
```

---

## 🧪 Test Scenarios

### ✅ Concurrent Update Test

**Class:** `UserConcurrencyTest`

What it validates:
- Multiple threads attempt to update the same user simultaneously
- Threads are synchronized to start together
- Only one update succeeds
- Remaining updates fail due to optimistic locking

This test uses:
- Real database
- Real transactions
- Real Spring context

➡️ Accurately simulates real-world concurrent users.

---

## ▶️ How to Run

### Run the application
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### H2 Console
```
http://localhost:8080/h2-console
```

---

## 📌 Sample API Endpoints

```http
GET  /users/{id}
PUT  /users/{id}
```

Use Postman, JMeter, or k6 to simulate concurrent access.

---

## ✅ Key Takeaways

- Demonstrates production-grade concurrency handling
- Clean separation of concerns
- Realistic resilience patterns
- Suitable for learning, interviews, and architecture discussions

---

## 📘 Documentation Note

📌 **Detailed explanations are provided directly inside the source code files** to ensure clarity and ease of understanding.

---

Happy coding 🚀
