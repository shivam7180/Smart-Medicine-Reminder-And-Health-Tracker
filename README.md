# 🏥 MediCare – Enterprise Smart Medicine Reminder & Health Tracker

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![React](https://img.shields.io/badge/React-18-61dafb.svg)](https://react.dev/)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38bdf8.svg)](https://tailwindcss.com/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ed.svg)](https://docs.docker.com/compose/)

**MediCare** is an enterprise-grade, distributed healthcare management platform designed with **Microservices Architecture**, **Database-per-Service** isolation, **Spring Cloud API Gateway & Eureka Service Discovery**, and a modern **React.js (Tailwind CSS + Recharts)** frontend.

It automates patient medication adherence tracking with interactive **Take / Skip / Snooze** actions, automated missed dose alerts, vital signs monitoring (Blood Pressure, Glucose, Heart Rate, SpO2, Temperature, Sleep, Hydration), automatic **BMI calculation**, doctor consultation scheduling, and **PDF / CSV clinical report downloads**.

---

## 🏛 1. Microservices Architecture Diagram

```mermaid
flowchart TB
    subgraph ClientLayer["Frontend Client Layer"]
        UI["React.js Healthcare Dashboard (Port: 3000)<br/>• Patient, Doctor & Admin Views<br/>• Recharts Biometrics Trends<br/>• Take / Skip / Snooze Actions"]
    end

    subgraph GatewayLayer["Edge & Gateway Routing"]
        GW["Spring Cloud API Gateway (Port: 8080)<br/>• Route Matching<br/>• JWT Validation Filter<br/>• User Context Injection (X-User-Id, X-User-Roles)<br/>• Global CORS & Error Envelopes"]
    end

    subgraph DiscoveryLayer["Service Registry"]
        Eureka["Eureka Discovery Server (Port: 8761)<br/>Dynamic Service Registry & Health Checks"]
    end

    subgraph MicroservicesCore["Microservices Core (Java 17 / Spring Boot 3.2.5)"]
        AuthSvc["Auth & User Service (8081)<br/>• BCrypt & JWT Tokens<br/>• Roles: PATIENT, DOCTOR, ADMIN"]
        MedSvc["Medicine Service (8082)<br/>• Catalog & Inventories<br/>• Low-Stock Alarms & Prescriptions"]
        RemSvc["Reminder Service (8083)<br/>• Daily Dose Engine<br/>• Overdue Missed Detector<br/>• 30-Day Adherence %"]
        HealthSvc["Health Tracking Service (8084)<br/>• Vitals (BP, Sugar, HR, SpO2)<br/>• Auto BMI Computation<br/>• Health Goals"]
        NotifSvc["Notification Service (8085)<br/>• In-App Alerts & Emails<br/>• Delivery History & Preferences"]
        ApptSvc["Doctor & Appointment Service (8086)<br/>• Doctor Specializations<br/>• Consultations & Rescheduling"]
        AnalyticsSvc["Analytics & Reporting Service (8087)<br/>• Composite Dashboard Telemetry<br/>• OpenPDF Medical Report Export<br/>• CSV Adherence Exports"]
        AdminSvc["Admin Service (8088)<br/>• User Account Governance<br/>• Security Audit Logs"]
    end

    subgraph DatabaseLayer["MySQL 8 (Isolated Schemas per Microservice)"]
        DB_Auth[(medicare_auth)]
        DB_Med[(medicare_medicine)]
        DB_Rem[(medicare_reminder)]
        DB_Health[(medicare_health)]
        DB_Notif[(medicare_notification)]
        DB_Appt[(medicare_appointment)]
        DB_Analytics[(medicare_analytics)]
        DB_Admin[(medicare_admin)]
    end

    UI -->|All HTTP REST Calls| GW
    GW -->|Forward with Auth Headers| MicroservicesCore
    MicroservicesCore <-->|Heartbeats & Discovery| Eureka

    AuthSvc --> DB_Auth
    MedSvc --> DB_Med
    RemSvc --> DB_Rem
    HealthSvc --> DB_Health
    NotifSvc --> DB_Notif
    ApptSvc --> DB_Appt
    AnalyticsSvc --> DB_Analytics
    AdminSvc --> DB_Admin

    RemSvc -.->|OpenFeign: Deduct Stock| MedSvc
    RemSvc -.->|OpenFeign: Dose Alerts| NotifSvc
    AnalyticsSvc -.->|OpenFeign Aggregation| RemSvc
    AnalyticsSvc -.->|OpenFeign Catalog| MedSvc
    AnalyticsSvc -.->|OpenFeign Vitals| HealthSvc
    AnalyticsSvc -.->|OpenFeign Consultations| ApptSvc
```

---

## 🔌 2. Service Port & Routing Mapping

| Microservice | Port | Database Schema | Gateway Path Route | Swagger UI Route |
| :--- | :---: | :--- | :--- | :--- |
| **API Gateway** | `8080` | None | `/` (Edge Ingress) | `http://localhost:8080/actuator` |
| **Eureka Server** | `8761` | None | Service Registry | `http://localhost:8761` |
| **Auth & User Service** | `8081` | `medicare_auth` | `/api/v1/auth/**`, `/api/v1/users/**` | `http://localhost:8081/swagger-ui.html` |
| **Medicine Service** | `8082` | `medicare_medicine` | `/api/v1/medicines/**`, `/api/v1/categories/**`, `/api/v1/inventory/**` | `http://localhost:8082/swagger-ui.html` |
| **Reminder Service** | `8083` | `medicare_reminder` | `/api/v1/schedules/**`, `/api/v1/doses/**` | `http://localhost:8083/swagger-ui.html` |
| **Health Tracking Service** | `8084` | `medicare_health` | `/api/v1/health/**`, `/api/v1/goals/**` | `http://localhost:8084/swagger-ui.html` |
| **Notification Service** | `8085` | `medicare_notification` | `/api/v1/notifications/**`, `/api/v1/notification-preferences/**` | `http://localhost:8085/swagger-ui.html` |
| **Doctor & Appointment** | `8086` | `medicare_appointment` | `/api/v1/doctors/**`, `/api/v1/appointments/**` | `http://localhost:8086/swagger-ui.html` |
| **Analytics & Reports** | `8087` | `medicare_analytics` | `/api/v1/analytics/**`, `/api/v1/reports/**` | `http://localhost:8087/swagger-ui.html` |
| **Admin Service** | `8088` | `medicare_admin` | `/api/v1/admin/**` | `http://localhost:8088/swagger-ui.html` |
| **React Web Application**| `3000` | N/A (Frontend) | Web UI | `http://localhost:3000` |

---

## 🗄 3. Database ER Model & Schema Design

Each service maintains strict schema isolation. Cross-database foreign keys are prohibited; cross-boundary coordination is handled via entity IDs and OpenFeign REST communication.

```mermaid
erDiagram
    %% Auth Service
    USERS ||--o{ REFRESH_TOKENS : has
    USERS }o--o{ ROLES : assigned
    USERS ||--o{ PASSWORD_RESET_TOKENS : generates

    %% Medicine Service
    MEDICINE_CATEGORIES ||--o{ MEDICINES : classifies
    MEDICINES ||--|| MEDICINE_INVENTORY : tracks_stock
    MEDICINES ||--o{ PRESCRIPTIONS : documented_in

    %% Reminder Service
    MEDICINE_SCHEDULES ||--|{ SCHEDULE_TIMES : triggers_at
    MEDICINE_SCHEDULES ||--o{ DOSE_LOGS : records

    %% Health Service
    USERS ||--o{ HEALTH_RECORDS : logs
    USERS ||--o{ HEALTH_GOALS : targets

    %% Appointment Service
    DOCTOR_SPECIALIZATIONS ||--o{ DOCTORS : specializes_in
    DOCTORS ||--o{ APPOINTMENTS : conducts
```

---

## 🔑 4. Demo Login Credentials

The database seed script (`database/seed-data.sql`) pre-loads realistic healthcare records and test accounts:

| Role | Email Address | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Patient** | `john.doe@medicare.com` | `Password@123` | Active medicines, dose reminders, vital logs, booking doctors, PDF clinical exports |
| **Doctor** | `dr.sarah.connor@medicare.com` | `Password@123` | Patient consultation schedule, appointment reviews, medical notes |
| **Admin** | `admin@medicare.com` | `Password@123` | Platform oversight, system audit trail, account activate/deactivate governance |

---

## 🚀 5. How to Run Locally

### Prerequisites
1. **Java 17 or 21**
2. **Node.js 18+** & **npm**
3. **MySQL 8.0+** running on `localhost:3306` (or Docker)
4. **Apache Maven 3.8+**

### Step 1: Initialize Database Schemas & Seed Data
Execute the provided initialization SQL scripts in your MySQL 8 instance:
```bash
mysql -u root -p < database/init-schemas.sql
mysql -u root -p < database/seed-data.sql
```

### Step 2: Build All Spring Boot Microservices
From the project root:
```bash
cd backend
mvn clean install -DskipTests
```

### Step 3: Start the Backend Services (Recommended Order)
Launch the microservices in separate terminal windows:
1. **Eureka Server**:
   ```bash
   cd backend/eureka-server && mvn spring-boot:run
   ```
   *(Verify Eureka registry is online at `http://localhost:8761`)*

2. **API Gateway**:
   ```bash
   cd backend/api-gateway && mvn spring-boot:run
   ```
   *(Edge router online at `http://localhost:8080`)*

3. **Core Microservices**:
   ```bash
   cd backend/auth-service && mvn spring-boot:run
   cd backend/medicine-service && mvn spring-boot:run
   cd backend/reminder-service && mvn spring-boot:run
   cd backend/health-service && mvn spring-boot:run
   cd backend/notification-service && mvn spring-boot:run
   cd backend/appointment-service && mvn spring-boot:run
   cd backend/analytics-service && mvn spring-boot:run
   cd backend/admin-service && mvn spring-boot:run
   ```

### Step 4: Start the React Frontend Application
```bash
cd frontend
npm install
npm run dev
```
Open **`http://localhost:3000`** in your browser.

---

## 🐳 6. Multi-Container Docker Deployment

To launch the entire platform (MySQL, Eureka, Gateway, all 8 microservices, and React Frontend) with a single command:

```bash
docker-compose up --build
```

To stop and tear down:
```bash
docker-compose down -v
```

---

## 🧪 7. Running Unit & Integration Tests

The platform includes comprehensive **JUnit 5** and **Mockito** unit tests covering:
- Authentication & JWT generation
- Duplicate user registration guards
- Medicine inventory threshold detection
- Dose action execution (Take / Skip / Snooze)
- Mathematical Adherence rate calculation: $\frac{\text{Taken}}{\text{Scheduled}} \times 100$
- Automatic abnormal biometric detection (Systolic > 140 / Diastolic > 90)

Run all tests:
```bash
cd backend
mvn test
```

---

## 📄 8. License & Notice
Developed for healthcare medication management and adherence tracking.
*Clinical Notice: This software provides personal adherence tracking and health metric logging. It does not replace professional medical diagnosis, prescription advice, or emergency treatment.*
