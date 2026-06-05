# TeamTasker — Full-Stack Team Task Manager

A full-stack web application for managing projects, assigning tasks, and tracking progress with role-based access control.

**Live URL:** `adorable-happiness-production-b581.up.railway.app`

---

## Features

- **Authentication** — JWT-based signup and login with BCrypt password hashing
- **Projects** — Create projects; creator automatically becomes Admin
- **Team Management** — Invite members by email, assign Admin or Member roles
- **Task Management** — Create, assign, and track tasks with a Kanban board (To Do / In Progress / Done)
- **Role-Based Access Control** — Admins manage everything; Members can only update status of their own assigned tasks
- **Overdue Detection** — Tasks past their due date are highlighted in red
- **Dashboard** — Stats overview: total projects, total tasks, completion rate, overdue list, and status chart

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3, Spring Security |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Auth | JWT (jjwt) + BCrypt |
| Frontend | React 18, Vite, TailwindCSS |
| Charts | Recharts |
| HTTP | Axios |
| Deployment | Railway |

---

## Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL running locally

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/team-task-manager.git
cd team-task-manager
```

### 2. Set up the database
```sql 
psql -U postgres
CREATE DATABASE taskmanager;
```

### 3. Configure backend environment
```bash
cd backend
cp .env.example .env
# Edit .env with your DB credentials and a JWT secret
```

### 4. Run backend
```bash
cd backend
mvn spring-boot:run
# Backend starts on http://localhost:8080
```

### 5. Run frontend (development)
```bash
cd frontend
npm install
npm run dev
# Frontend starts on http://localhost:5173 with API proxy to :8080
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/taskmanager` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | JWT signing secret (32+ chars) | dev default |
| `PORT` | Server port | `8080` |
| `JAVA_OPTS` | JVM flags | `-Xmx256m` |

---

## API Summary

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/signup` | No | Register |
| POST | `/api/auth/login` | No | Login → JWT |
| GET | `/api/auth/me` | Yes | Current user |
| GET | `/api/projects` | Yes | List my projects |
| POST | `/api/projects` | Yes | Create project |
| GET/PATCH/DELETE | `/api/projects/:id` | Yes | Manage project |
| GET/POST | `/api/projects/:id/members` | Yes | List/invite members |
| PATCH/DELETE | `/api/projects/:id/members/:uid` | Admin | Change role/remove |
| GET/POST | `/api/projects/:id/tasks` | Yes | List/create tasks |
| PATCH | `/api/projects/:id/tasks/:tid` | Admin | Full task edit |
| PATCH | `/api/projects/:id/tasks/:tid/status` | Admin or Assignee | Update status only |
| DELETE | `/api/projects/:id/tasks/:tid` | Admin | Delete task |
| GET | `/api/dashboard` | Yes | Aggregated stats |
| GET | `/api/health` | No | Health check |

---
#
