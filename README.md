# Order Service — Orchestration, Saga & Kafka Reliability (v3)

> Cloud-native order orchestration microservice responsible for order creation, saga coordination, Kafka-based event publishing, payment result handling, Dead Letter Topic management, resilience patterns, and distributed transaction consistency.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Kafka](https://img.shields.io/badge/Kafka-Event--Driven-black)
![DLT](https://img.shields.io/badge/Kafka-DLT-critical)
![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)
![Tests](https://img.shields.io/badge/Tests-Repository%20%7C%20Unit%20%7C%20Slice%20%7C%20Integration-informational)
![Observability](https://img.shields.io/badge/Observability-OpenTelemetry-purple)
![Docker](https://img.shields.io/badge/Docker-Supported-2496ED)

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture Role](#architecture-role)
- [Communication Model](#communication-model)
- [Saga Flow](#saga-flow)
- [Kafka Reliability Model](#kafka-reliability-model)
- [Dead Letter Topic (DLT)](#dead-letter-topic-dlt)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Kafka Publishing Design](#kafka-publishing-design)
- [Validation & Error Handling](#validation--error-handling)
- [Resilience Design](#resilience-design)
- [Security](#security)
- [Configuration](#configuration)
- [Run Locally](#run-locally)
- [Run with Docker](#run-with-docker)
- [Testing Strategy](#testing-strategy)
- [Observability](#observability)
- [Version History](#version-history)
- [Roadmap](#roadmap)
- [Author](#author)

---

## Overview

The **Order Service** manages the **order lifecycle** inside the platform and acts as the **workflow orchestrator** of business operations.

It combines:

- synchronous service-to-service communication
- asynchronous Kafka communication
- saga-based distributed transaction management
- resilient order event publishing
- dead letter topic handling for failed event processing

This service is part of the **Cloud Native Architecture v3** and is designed to be:

- scalable
- resilient
- observable
- event-driven
- production-oriented

---

## Key Features

- Order creation and persistence
- Saga-based orchestration
- Kafka event publishing for order workflows
- Payment success and payment failure handling
- Compensation-based consistency
- Decoupled Kafka publishing through a dedicated `KafkaPublish` component
- Custom exception for Kafka publishing failures
- Dead Letter Topic support for failed Kafka processing
- Warning logs when a payment event references a non-existing order
- Repository, unit, slice, and integration test coverage
- Dockerized execution support

---

## Architecture Role

The Order Service acts as the **orchestrator of the business workflow**.

### Responsibilities

- create and persist orders
- initiate distributed order workflows
- publish order events to Kafka
- react to payment outcomes
- confirm or cancel orders
- support failed-message recovery with DLT
- enforce consistency through Saga compensation
- coordinate synchronous calls to supporting services

### Position in the system

```text
Client
  ↓
API Gateway
  ↓
Order Service
   ├─ (Sync) → Other Services
   └─ (Async) → Kafka → Payment Service
````

The Order Service bridges synchronous business validation with asynchronous payment processing.

---

## Communication Model

The Order Service uses a **hybrid communication model**.

### Synchronous Communication

Used when immediate validation is required before continuing the workflow, such as:

* user validation
* stock checks
* dependency lookups

Typically handled with:

* Feign Client
* Resilience4j
* fallback strategies

### Asynchronous Communication

Used when the workflow should continue without blocking, especially for:

* payment initiation
* payment result handling
* eventual consistency
* retry and failed-message handling

Handled through:

* Kafka topics
* event consumers
* dead letter topics

---

## Saga Flow

The Order Service follows an **orchestration-based Saga pattern**.

### Main flow

```text
1. Create Order as PENDING
2. Publish order event to Kafka
3. Wait for payment result
   ├─ Payment success → CONFIRMED
   └─ Payment failure → CANCELLED
```

### Goal

Because orders and payments span multiple services and databases, a traditional ACID transaction is not possible.

The Saga pattern ensures:

* eventual consistency
* explicit compensation
* fault isolation
* safe state transitions

---

## Kafka Reliability Model

Kafka is used as the backbone of the asynchronous workflow.

### Reliability goals

* decouple services
* avoid blocking business flows
* handle transient failures
* isolate poison messages
* preserve observability of failed events

### Event flow

```text
Order Service
   ↓
Main Kafka Topic
   ↓
Consumer processing
   ├─ success → normal workflow continues
   └─ failure → Dead Letter Topic
```

The service uses Kafka not only for communication, but also for **failure isolation and recovery visibility**.

---

## Dead Letter Topic (DLT)

The Order Service supports **Dead Letter Topic handling** for Kafka messages that cannot be processed successfully.

### Why DLT is needed

In distributed systems, some events may fail permanently because of:

* malformed payloads
* unexpected business state
* downstream inconsistencies
* non-recoverable processing errors

Instead of endlessly retrying or silently losing messages, failed events are redirected to a **Dead Letter Topic**.

### DLT purpose

The DLT provides:

* failure isolation
* safer event processing
* easier debugging
* replay/recovery possibilities
* better production visibility

### Conceptual flow

```text
orders-topic
   ↓
Order consumer
   ├─ success → business flow continues
   └─ failure → orders-topic-dlt
```

### Operational value

DLT messages can later be:

* inspected manually
* replayed after correction
* used for alerting and incident analysis
* tracked in observability dashboards

This makes the event-driven workflow much more robust in production environments.

---

## Tech Stack

* **Java 17+**
* **Spring Boot 3.x**
* **Spring Data JPA**
* **Spring Kafka**
* **Kafka**
* **Resilience4j**
* **Feign Client**
* **JUnit 5**
* **Mockito**
* **MockMvc**
* **Awaitility**
* **Testcontainers**
* **spring-kafka-test**
* **spring-security-test**
* **spring-cloud-contract-wiremock**
* **jackson-databind**
* **Docker**

---

## Project Structure

```text
src
 ├── main
 │   ├── java/com/microtest
 │   │   ├── event
 │   │   └── OrderService
 │   │       ├── config
 │   │       ├── controller
 │   │       ├── dto
 │   │       ├── entity
 │   │       ├── enums
 │   │       ├── exception
 │   │       ├── feign
 │   │       ├── kafka
 │   │       ├── repository
 │   │       └── service
 │   │           └── impl
 │   └── resources
 │       ├── application.yml
 │       └── bootstrap.yaml 
 │
 └── test
     ├── java/com/microtest/OrderService
     │   ├── controller
     │   ├── integration
     │   ├── repository
     │   ├── service
     │   └── support
     └── resources
         └── application-test.yml      
```

---

## Domain Model

### Order Entity

An order typically contains:

* order id
* user reference
* business data
* status
* timestamps

### Order Status

| State       | Description                               |
| ----------- | ----------------------------------------- |
| `PENDING`   | Order created, waiting for payment result |
| `CONFIRMED` | Payment succeeded                         |
| `CANCELLED` | Payment failed or compensation executed   |

---

## API Endpoints

### Create Order (Saga flow)

```http
POST /saga
```

Starts the saga workflow.

### Behavior

* creates the initial order state
* publishes the order event to Kafka
* returns:

  * `200 OK` if the event is published successfully
  * `400 Bad Request` if the Kafka event is not published

### Create / Publish Order

```http
POST /order
```

Publishes the order flow through Kafka using the decoupled publisher component.

### Behavior

* triggers order event publication
* returns:

  * `200 OK` if the Kafka event is published successfully
  * `400 Bad Request` if the Kafka event is not published

---

## Kafka Publishing Design

A major improvement in this version is the introduction of the **`KafkaPublish`** component.

### Why this component?

The goal is to keep **business logic independent of Kafka internals**.

Instead of embedding Kafka-specific code directly inside services, publishing is delegated to a dedicated component.

### Benefits

* cleaner service layer
* better separation of concerns
* easier unit testing
* easier support for multiple topics and event types
* better maintainability

### Design intention

```text
Controller
  ↓
Service
  ↓
KafkaPublish
  ↓
KafkaTemplate / Kafka internals
```

This prevents service methods from becoming tightly coupled to Kafka implementation details.

---

## Validation & Error Handling

### Clean Code Refactor

The original `createOrderWithSagaPattern` logic was split into smaller functions so that each method handles a single responsibility.

This improves:

* readability
* maintainability
* testability
* adherence to clean code principles

### Custom Exception

#### `KafkaErrorPublishException`

Thrown when a Kafka event is expected to be published but the publishing operation fails.

Used in:

* saga creation flow
* direct order publishing flow

### Controller behavior

Endpoints now return a more explicit HTTP response based on publish success:

| Endpoint | Success  | Failure           |
| -------- | -------- | ----------------- |
| `/saga`  | `200 OK` | `400 Bad Request` |
| `/order` | `200 OK` | `400 Bad Request` |

### Event handling safeguards

The payment event handlers write a **warning log** if a payment success or payment failure event is received for an order that does not exist in the database.

This prevents silent inconsistencies and improves diagnosis in asynchronous flows.

---

## Resilience Design

The Order Service uses resilience mechanisms to prevent cascading failures.

### Patterns used

* Circuit Breaker
* Retry
* Fallback methods
* Dead Letter Topic handling

### Why?

To protect the workflow when dependent services are:

* temporarily unavailable
* slow
* partially degraded
* sending invalid or unprocessable events

### Example behavior

```text
Dependent service failure
   ↓
Retry attempts
   ↓
Circuit opens if threshold reached
   ↓
Fallback executed
```

For Kafka consumers:

```text
Event processing failure
   ↓
Retry / error handling
   ↓
DLT redirection
```

This makes the system safer and easier to operate under failure conditions.

---

## Security

* The Order Service is not intended for direct public exposure
* It is accessed through the API Gateway
* User identity is propagated through JWT validated at the gateway
* Test support includes `spring-security-test`

---

## Configuration

This service is designed to retrieve configuration from a **Config Server** in cloud environments.

Typical externalized properties include:

* datasource configuration
* Kafka bootstrap servers
* topic names
* DLT topic names
* Feign client configuration
* resilience settings
* server port
* service registration settings

### Test Configuration

For tests, external dependencies such as Config Server should be disabled.

Example:

```yaml
spring:
  cloud:
    config:
      enabled: false
```

---

## Run Locally

### Prerequisites

* Java 17+
* Maven
* Running database
* Kafka broker
* Config Server running if required by your environment
* Discovery service running if required by your environment
* Dependent services available if testing full workflows

### Start the service

```bash
mvn clean spring-boot:run
```

### Run tests

```bash
mvn test
```

---

## Run with Docker

### Build the jar

```bash
mvn clean package
```

### Build the Docker image

```bash
docker build -t order-service:3.0 .
```

### Run the container

```bash
docker run -p 8082:8082 order-service:3.0
```

### Example with environment variables

```bash
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  order-service:3.0
```

If your architecture uses Config Server, Discovery, Kafka, or other dependencies, make sure the container can reach them through the correct network configuration.

---

## Testing Strategy

This service includes several levels of tests.

### Repository Tests

Verify:

* persistence logic
* state transitions
* repository queries

### Unit Tests

Verify:

* order creation logic
* saga decomposition
* exception handling
* Kafka publishing decision flow
* clean service boundaries

### Slice Tests

Verify:

* controller mappings
* HTTP status codes
* JSON request/response contracts
* controller error handling

### Integration Tests

Verify:

* full Spring context behavior
* Kafka event publishing
* endpoint-to-Kafka workflow
* asynchronous communication flow
* DLT routing behavior
* dependent service stubbing when needed

### Integration test dependencies

* `spring-kafka-test`
* `testcontainers`
* `junit-jupiter`
* `awaitility`
* `spring-cloud-contract-wiremock`

### Test coverage includes DLT

The integration layer can validate:

* main topic publishing
* failed event routing to DLT
* warning/error scenarios in asynchronous consumers

---

## Observability

The service is designed to integrate with a cloud-native observability stack:

* **OpenTelemetry**
* **Prometheus**
* **Loki**
* **Tempo**
* **Grafana**
* **Kafka metrics and traces**

This supports:

* end-to-end saga tracing
* Kafka flow visibility
* DLT monitoring
* retry and fallback monitoring
* payment success/failure tracking
* warning/error diagnosis

DLT visibility is particularly important for identifying poisoned messages and non-recoverable processing failures in production.

---

## Version History

### v3

* Split `createOrderWithSagaPattern` into smaller functions for cleaner code
* Added `KafkaErrorPublishException`
* Added `KafkaPublish` component to isolate Kafka internals from business logic
* Updated saga creation flow to throw `KafkaErrorPublishException` when publish fails
* Updated `/saga` controller to return `200` on publish success and `400` on publish failure
* Updated direct order publishing flow to throw `KafkaErrorPublishException` when publish fails
* Updated `/order` controller to return `200` on publish success and `400` on publish failure
* Updated `handlePaymentSuccess` and `handlePaymentFailed` to log warnings when the order does not exist
* Integrated Dead Letter Topic handling into the event-driven reliability model
* Added repository tests
* Added unit tests
* Added controller slice tests
* Added integration test dependencies
* Added integration tests
* Added Docker support

### v2

* Added Principe of Clean Code

### v1

* Initial service foundation
* Initial order creation flow
* Kafka-based payment interaction
* Saga handling foundation

---

## Roadmap

* Add OpenAPI / Swagger documentation
* Add CI/CD pipeline badges
* Add contract tests between Order and Payment services
* Add advanced saga monitoring dashboards
* Add DLT replay/reprocessing strategy
* Add production deployment manifests

---

## Author

**Bah Youne**

Founder & Backend / Full Stack Java Developer

* GitHub: [http://github.com/bahyoune]
* LinkedIn: [http://linkedin.com/in/younoussa-bah]

---

## License

This project is shared for educational, portfolio, and demonstration purposes unless specified otherwise.



