
# 🛠️ Scriven Backend – Developer Documentation

## Overview

The Scriven backend is a Spring Boot application powering an enterprise collaboration platform that enables team communication, project alignment, attendance tracking, and AI assistance.

> ❗ **Note:** This repository does **not accept contributions**. However, you are welcome to **fork** it and use the code in your own projects.

---

## 🚀 Tech Stack

* **Framework:** Spring Boot 3.x
* **Build Tool:** Maven
* **Database:** PostgreSQL
* **Security:** Spring Security & JWT
* **AI Integration:** Maya (Mobile Assistant for Your Achievements)
* **Messaging:** Stream Chat API
* **Notifications:** Email (SMTP)
* **File Storage:** Google Drive integration

---

## 📁 Project Structure

```bash
src/
├── main/
│   ├── java/com/odin360/
│   │   ├── controllers/       # REST Controllers
│   │   ├── services/          # Business Logic
│   │   ├── repositories/      # JPA Interfaces
│   │   ├── domains/           # Entity Models
│   │   └── config/            # Security & App Config
│   └── resources/
│       ├── application.yml    # Config File
└── test/                      # Unit & Integration Tests
```

---

## ⚙️ Setup Instructions

### Prerequisites

* Java 17+
* Maven 3.8+
* PostgreSQL
* SMTP Credentials

### Local Setup

```bash
git clone https://github.com/Odin360/Odin-360-backend.git
cd Odin-360-backend
```

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/scriven
    username: your_db_user
    password: your_db_pass

mail:
  host: smtp.yourhost.com
  username: your_email
  password: your_email_password
```

Then run:

```bash
mvn spring-boot:run
```

---

## 🔐 Security

* JWT Authentication for protected routes
* Role-based authorization
* CORS configured for frontend access
* CSRF protection disabled for token use

---

## 📦 Packaging

```bash
mvn clean install
java -jar target/Odin-360-backend-0.0.1-SNAPSHOT.jar
```

---

## 📡 REST API Endpoints (Examples)

### 🧑 User

| Method | Endpoint             | Description                |
| ------ | -------------------- | -------------------------- |
| POST   | `/api/v1/auth/signUp` | Register new user          |
| POST   | `/api/v1/auth/login`    | Login and receive JWT      |
| GET    | `/api/v1/users/user`  | Fetch current user profile |
| PUT    | `/api/users/user/{id}`   | Update user info           |

### 👥 Team

| Method | Endpoint             | Description                   |
| ------ | -------------------- | ----------------------------- |
| POST   | `/api/v1/teams/create`   | Create a new team             |
| GET    | `/api/v1/teams/{id}`     | Get team details              |
| POST    | `/api/v1/teams/team/{id}` | Add member to team            |
| GET    | `/api/v1/teams/team/` | List teams current user is in |

### 🤖 Maya (AI Assistant)

| Method | Endpoint        | Description            |
| ------ | --------------- | ---------------------- |
|GET   | `/api/maya/v1/{channelid}/{userId}/askAi?prompt={prompt}` | Ask a question to Maya |

### 📂 Images

| Method | Endpoint            | Description                     |
| ------ | ------------------- | ------------------------------- |
| POST   | `/api/v1/images/upload`   | Upload image to image folder |
| GET    |  `/api/v1/images/download` | Download image from image folder     |


## 🧠 Features

* JWT-secured user authentication
* Team management and team-based access control
* Integration with Google Drive for file uploads
* Stream Chat integration for communication
* Maya AI assistant (NLP-based responses)
* Attendance and productivity tracking

---

## 📄 License & Contribution

* **License:** MIT
* **Contributions:** Not accepted.
* **Usage:** You are free to **fork** and use this code in your own projects.



