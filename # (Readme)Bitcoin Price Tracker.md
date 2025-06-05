# Bitcoin Price Tracker

A full-stack project to fetch, cache, and visualize historical Bitcoin prices using a **Spring Boot** backend and a **React (Vite)** frontend. The system supports both live and offline (cached) data, with robust CI/CD and Docker support.

---

## 🛠️ System Design: Step-by-Step

### 1. **Project Structure**
```
bitcoinproject/
├── bitcoin-price/         # Spring Boot backend (Java 17, Spring MVC)
│   ├── src/
│   ├── Dockerfile
│   └── ...
├── frontend/              # React frontend (Vite)
│   ├── src/
│   ├── Dockerfile
│   └── ...
└── .github/
    └── workflows/
        └── ci.yml        # GitHub Actions CI/CD pipeline
```

---

### 2. **Backend (Spring Boot)**
- **Exposes**: `/api/v1/prices?start=YYYY-MM-DD&end=YYYY-MM-DD&offline=false`
- **Features**:
  - Fetches Bitcoin prices from the Coindesk API.
  - Caches results for offline/fallback mode.
  - Uses Resilience4j for circuit breaker/fallback.
  - Validates date input and handles errors gracefully.
- **Dockerized**: Uses a multi-stage Dockerfile for efficient builds.

---

### 3. **Frontend (React + Vite)**
- **Features**:
  - Date pickers for selecting start/end dates.
  - Option to fetch in offline mode.
  - Displays prices in a table, highlighting high/low points.
  - Handles and displays errors from the backend.
- **Dockerized**: Builds static files and serves via Nginx.

---

### 4. **CI/CD Pipeline**
- **GitHub Actions**:
  - On push/PR to `main`, runs backend and frontend builds/tests.
  - Builds and pushes Docker images for both services.
  - Placeholder for deployment (customize as needed).

---

## 🚀 How to Run Locally

### **Prerequisites**
- [Node.js](https://nodejs.org/) (v18+)
- [Java 17+](https://adoptopenjdk.net/)
- [Maven](https://maven.apache.org/) (or use `./mvnw`)
- [Docker](https://www.docker.com/) (optional, for containerization)

---

### **1. Clone the Repository**
```sh
git clone https://github.com/MD2105/bitcoinproject.git
cd bitcoinproject
```

---

### **2. Start Both Apps (Dev Mode)**
#### **With a single command (recommended):**
```sh
cd frontend
npm install
npm run start:all
```
- This uses `concurrently` to run both the React frontend and Spring Boot backend.

#### **Or, run separately:**

**Backend:**
```sh
cd bitcoin-price
./mvnw spring-boot:run
```

**Frontend:**
```sh
cd frontend
npm install
npm run dev
```

---

### **3. Access the App**
- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080/api/v1/prices](http://localhost:8080/api/v1/prices)

---

### **4. Run with Docker (Optional)**
**Build and run backend:**
```sh
cd bitcoin-price
docker build -t your-dockerhub-username/bitcoin-backend:latest .
docker run -p 8080:8080 your-dockerhub-username/bitcoin-backend:latest
```

**Build and run frontend:**
```sh
cd frontend
docker build -t your-dockerhub-username/bitcoin-frontend:latest .
docker run -p 80:80 your-dockerhub-username/bitcoin-frontend:latest
```

---

## ⚙️ CI/CD

- On every push/PR to `main`, GitHub Actions will:
  - Build and test backend and frontend
  - Build and push Docker images (if Docker credentials are set in repo secrets)
  - (Optional) Deploy to your environment (customize `ci.yml`)

---

## 📝 Notes

- **Date format:** Always use `YYYY-MM-DD` for API requests.
- **Offline mode:** Uses cached data if available, or returns an error if not.
- **Secrets:** For Docker image push, set `DOCKER_USERNAME` and `DOCKER_PASSWORD` (or token) in GitHub repo secrets.

---