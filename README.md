# Order Service

The **Order Service** is responsible for managing the **order lifecycle** in the platform.

It acts as the **orchestrator** of business workflows, combining:

* **Synchronous communication** with other services
* **Asynchronous communication** via Kafka
* **Resilience patterns** (Circuit Breaker & Retry)
* **Saga Pattern** for distributed transactions

This service is designed to be **fault-tolerant, scalable, and observable**.

---

## Responsibilities

The Order Service handles:

* Order creation and validation
* Synchronous calls to external services (e.g. User, Inventory)
* Asynchronous communication with Payment Service
* Saga orchestration and state management
* Retry and fallback strategies
* Order compensation on failure

---

## Architecture Overview

The Order Service uses a **hybrid communication model**:

```
Client
  ↓
API Gateway
  ↓
Order Service
   ├─ (Sync / Feign + Resilience) → Other Services
   └─ (Async / Kafka) → Payment Service
```

---

## Synchronous Communication (Feign + Resilience)

### Purpose

The Order Service uses **synchronous REST calls** when:

* Immediate data is required (e.g. user info, inventory check)
* Strong consistency is needed before creating an order

Feign is used as the HTTP client.

---

### Resilience Patterns

To prevent cascading failures, the following patterns are applied:

* **Circuit Breaker**
* **Retry**
* **Fallback methods**

Implemented using **Resilience4j**.

---

### Circuit Breaker

Protects the system from repeated failures.

* Opens when failures exceed a threshold
* Short-circuits calls to failing services
* Automatically recovers when the service becomes healthy

Example behavior:

```
Service down → Circuit OPEN → Fallback triggered
```

---

### Retry

Retries transient failures such as:

* Network glitches
* Temporary unavailability

Retries are:

* Limited
* Backoff-controlled
* Observed via metrics

---

### Fallback Strategy

If retries fail or the circuit is open:

* A fallback method is executed
* The order process either:

  * Fails gracefully
  * Or continues with partial data (depending on business rules)

---

## Asynchronous Communication (Kafka)

### Purpose

The Order Service communicates asynchronously with the **Payment Service** using Kafka.

This ensures:

* Loose coupling
* High scalability
* Non-blocking order creation
* Better fault tolerance

---

### Event Publishing

After an order is validated and created:

```
OrderCreatedEvent
```

is published to Kafka.

The Order Service does **not wait** for payment confirmation synchronously.

---

### Event Consumption

The Order Service listens for:

* Payment success events
* Payment failure events
* Compensation triggers

---

## Saga Pattern (Orchestration)

### Why Saga?

Orders and payments span multiple services and databases.
A traditional ACID transaction is **not possible**.

The Saga Pattern ensures:

* Data consistency
* Explicit compensation
* Event-driven coordination

---

### Saga Orchestration Model

The Order Service acts as the **Saga Orchestrator**.

```
1. Create Order (PENDING)
2. Publish OrderCreatedEvent
3. Wait for Payment Result
   ├─ SUCCESS → Confirm Order
   └─ FAILURE → Compensate Order
```

---

### Compensation Logic

If payment fails:

* Order is marked as `CANCELLED`
* Compensation logic is executed
* No partial or inconsistent state remains

This guarantees **eventual consistency**.

---

## Order States

| State     | Description                        |
| --------- | ---------------------------------- |
| PENDING   | Order created, payment in progress |
| CONFIRMED | Payment successful                 |
| CANCELLED | Payment failed / compensated       |

---

## Security

* Order Service is **not publicly exposed**
* Only accessible via API Gateway
* User identity is propagated via JWT (validated at gateway)

---

## Observability

The Order Service is fully observable using:

* **OpenTelemetry**
* **Prometheus** (metrics)
* **Loki** (Logs)
* **Tempo** (distributed tracing)
* **Grafana** (dashboards)
* **Kafka metrics & traces**

This allows:

* End-to-end tracing of an order saga
* Visibility into retries and circuit breaker states
* Kafka event flow monitoring

---

## Startup Order

The Order Service depends on:

1. Config Server
2. Eureka Discovery
3. Kafka Broker
4. Dependent services (User, etc.)

Once started, it registers with Eureka and becomes available via the API Gateway.

---

## Why this design?

This design follows **industry best practices**:

* Resilient synchronous calls
* Event-driven asynchronous workflows
* Explicit distributed transaction management
* Fault isolation and graceful degradation
* Scalable and cloud-native architecture

It mirrors patterns used in **real-world, high-scale systems**.

---

## Summary

* Order orchestration service
* Feign-based synchronous calls
* Circuit breaker & retry protection
* Kafka-based async communication
* Saga pattern with compensation
* Fully observable & resilient



