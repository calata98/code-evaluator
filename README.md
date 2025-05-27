# Code Evaluator

A backend project designed to automatically evaluate programming exercises submitted by users. This first phase establishes a minimal working pipeline using Apache Kafka, laying the foundation for a future intelligent feedback system.

## 🎯 Objective

The ultimate goal of this project is to build a backend platform that enables:

- ✅ Secure submission of programming exercises via a REST API.

- ⚙️ Execution of submitted code in isolated containers (Docker) to prevent malicious behavior.

- 🧠 Integration with AI-powered services to provide intelligent feedback on the submitted code, such as:

  - Code quality suggestions

  - Logic errors or inefficiencies

  - Best practices and improvement hints

- 🗃 Storage of submissions, evaluations, and feedback in a database.

- 🔐 User authentication and access control via JWT.

## 🧰 Technologies Used

- Java 21
- Spring Boot 3.2.6
- Apache Kafka
- Docker & Docker Compose
- Maven
- Lombok

## 📁 Project Structure

```
code-evaluator/
├── producer-api/        # REST API to send code submissions to Kafka
├── consumer-service/    # Kafka consumer that logs incoming messages
├── docker/              # Docker Compose setup with Kafka and Zookeeper
├── .gitignore
└── README.md
```

---

## 📌 Next Steps

- Implement code evaluation logic.
- Store submissions and results in a database.
- Add JWT authentication.
- Provide intelligent feedback using AI.

---

## 🧠 Author & License

Developed by Pablo Calatayud.  
This project is currently under active development.
