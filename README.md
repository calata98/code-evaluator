# 🚀 Code Evaluator Platform

**An Intelligent, Secure, Event‑Driven Backend for Automated Code
Assessment**

------------------------------------------------------------------------

## 🌐 Overview

Code Evaluator is a fully decoupled, microservice‑based backend platform
designed to automatically assess programming exercises.\
It integrates secure sandbox execution, similarity detection, authorship
verification, and AI‑generated feedback.

Originally developed as a Bachelor's Thesis project, it is now refined
and production‑ready for professional use and portfolio presentation.

------------------------------------------------------------------------

## 🎯 Key Capabilities

### 🔒 **Secure Code Execution**

-   Docker‑isolated sandbox per execution
-   Resource limits (memory, CPU, timeout)
-   Read‑only filesystem & restricted capabilities
-   Preventive protection against malicious code

### 🤖 **AI‑Powered Feedback**

Generates multi‑dimensional feedback (style, complexity, best practices)
using LLMs via **Spring AI**.

### 🧬 **Similarity Detection**

-   Token normalization\
-   SHA‑256 hashing\
-   SimHash 64‑bit\
-   N‑grams for structural analysis\
-   False‑positive rate \<5%

### 👤 **Authorship Verification**

Interactive quiz generated from patterns in the student's code to
confirm authorship.

### 📡 **Event‑Driven Orchestration**

All processing is asynchronous and decoupled using **Apache Kafka**.

### 🔐 **Authentication & Authorization**

-   JWT auth\
-   Role‑based access (Student / Teacher / Admin)

------------------------------------------------------------------------

## 🏗️ Architecture

    code-evaluator/
    ├── producer-api/                 # Public REST API for submissions
    ├── submission-api/               # Submission lifecycle + SSE
    ├── evaluation-orchestrator/      # Orchestrates all evaluation stages
    ├── executor-service/             # Docker sandbox runner
    ├── ai-feedback-service/          # AI feedback generator
    ├── similarity-service/           # Code similarity detection
    ├── authorship-service/           # Authorship verification tests
    ├── user-service/                 # Users, roles, authentication
    ├── contracts/                    # Shared Kafka event schemas
    └── docker/                       # Kafka, MongoDB, microservice stack

Designed using **Hexagonal Architecture** to ensure maintainability,
testability, and clean separation of concerns.

------------------------------------------------------------------------

## 🔄 Evaluation Flow

    [Submission] → Kafka → [Orchestrator]
        → Execution Request → [Executor Service]
        → Similarity Request → [Similarity Service]
        → AI Feedback Request → [AI Feedback Service]
        → Authorship Test → [Authorship Service]
    → Results Persisted & Exposed via REST/SSE

------------------------------------------------------------------------

## 🧰 Tech Stack

-   **Backend:** Java 21, Spring Boot 3.2\
-   **Async Messaging:** Apache Kafka\
-   **DB:** MongoDB\
-   **Security:** Spring Security + JWT\
-   **DevOps:** Docker, Docker Compose\
-   **AI:** Spring AI + LLM integrations\
-   **Testing:** JUnit 5, Mockito

------------------------------------------------------------------------

## 📦 Running the Platform

``` bash
docker compose --profile build-only up --build
```

All microservices, Kafka, MongoDB, and dependencies are launched
automatically.

------------------------------------------------------------------------

## 📌 Example Endpoints

### Submissions

    POST /submissions
    GET /submissions/{id}
    GET /submissions/{id}/events   # Real-time SSE

### Auth

    POST /login
    POST /register

### Teacher View

    GET /teacher/submissions
    GET /teacher/evaluations/{id}

------------------------------------------------------------------------

## 🧪 Testing Strategy

-   Complete unit tests (services, commands, mappers)
-   Integration tests for:
    -   Kafka messaging\
    -   Sandbox execution\
    -   API endpoints\
    -   MongoDB persistence\
-   Test doubles for AI + execution layers

------------------------------------------------------------------------

## 📈 Why This Project Matters (for Employers)

This project demonstrates:

### ✔️ **Advanced Backend Engineering**

Microservices, orchestration, event-driven design, DDD, and clean
architecture.

### ✔️ **Systems Thinking**

Handling of unsafe code execution, similarity algorithms, async
workflows, and distributed communication.

### ✔️ **AI Integration Skills**

Practical use of LLMs for automated feedback generation.

### ✔️ **DevOps Maturity**

Docker, container security, full-stack orchestration, reproducible
environments.

### ✔️ **Production-Ready Patterns**

-   Retry & error-handling with Kafka\
-   SSE for real-time UI\
-   Separation of concerns\
-   Domain-driven modeling

------------------------------------------------------------------------

## 👨‍💻 Author

**Pablo Calatayud**\
Backend Software Engineer\
Specialized in Java, Spring Boot, Distributed Systems & AI Integration.