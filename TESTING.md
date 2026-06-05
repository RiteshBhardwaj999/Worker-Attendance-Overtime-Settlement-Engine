# Testing Guide

## Prerequisites

- Backend running: `mvn spring-boot:run` from `backend/` folder
- `.env` file with Supabase + Redis credentials
- Port: `8080` (default, or `8081` if changed in `.env`)

---

## Method 1: Testing with Postman (Recommended for Windows)

Using Postman is the easiest way to test these APIs on Windows without dealing with terminal formatting and quote escaping issues.

### 1. Auth & Token Setup
1. Open Postman and create a new **POST** request to `http://localhost:8080/api/auth/signup`.
2. Go to the **Body** tab, select **raw**, and choose **JSON** from the dropdown.
3. Paste:
   ```json
   {
     "username": "admin",
     "email": "a@b.com",
     "password": "test123"
   }
   ```
4. Click **Send**.
5. Create another **POST** request to `http://localhost:8080/api/auth/login`. Use the same body as above (without username).
6. Copy the `token` string from the response.
7. For all the following requests, go to the **Authorization** tab, select **Bearer Token**, and paste your token.

### 2. Create Worker + Site
1. **POST** `http://localhost:8080/api/workers`
   - Body (raw JSON):
     ```json
     {
       "name": "Ramesh",
       "phone": "9876543210",
       "designation": "MASON",
       "dailyWageRate": 800
     }
     ```
   - *Copy the `id` from the response (this is your `workerId`).*

2. **POST** `http://localhost:8080/api/sites`
   - Body (raw JSON):
     ```json
     {
       "siteName": "Site A",
       "location": "Delhi"
     }
     ```
   - *Copy the `id` from the response (this is your `siteId`).*

### 3. Clock In & Clock Out
1. **POST** `http://localhost:8080/api/attendance/clock-in`
   - Body (raw JSON):
     ```json
     {
       "workerId": "PASTE_WORKER_ID_HERE",
       "siteId": "PASTE_SITE_ID_HERE",
       "clockInTime": "2025-05-01T07:00:00"
     }
     ```

2. **POST** `http://localhost:8080/api/attendance/clock-out`
   - Body (raw JSON):
     ```json
     {
       "workerId": "PASTE_WORKER_ID_HERE",
       "clockOutTime": "2025-05-01T18:00:00"
     }
     ```

### 4. Check Overtime Summary
1. **GET** `http://localhost:8080/api/overtime/summary/PASTE_WORKER_ID_HERE?month=2025-05`
2. Review the breakdown and calculated totals.

### 5. Settle Overtime
1. **POST** `http://localhost:8080/api/overtime/settle/PASTE_WORKER_ID_HERE?month=2025-05`

---

## Method 2: Testing via Terminal (Windows PowerShell / CMD)

**Windows PowerShell Users:** The built-in `curl` in PowerShell is an alias for `Invoke-WebRequest`, which causes the errors you saw. To use the real curl, you must type `curl.exe`, remove multi-line backslashes (`\`), and escape internal quotes using `\"`.

*Here are the correct single-line commands that work directly in Windows CMD or PowerShell:*

### 1. Health Check
```powershell
curl.exe http://localhost:8080/actuator/health
```
*(Expect `{"status":"UP"}`)*

### 2. Auth
```powershell
curl.exe -X POST http://localhost:8080/api/auth/signup -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"email\":\"a@b.com\",\"password\":\"test123\"}"

curl.exe -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"a@b.com\",\"password\":\"test123\"}"
```
*(Copy the token from the response to use in the following commands, replacing `YOUR_TOKEN`)*

### 3. Create Worker
```powershell
curl.exe -X POST http://localhost:8080/api/workers -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Ramesh\",\"phone\":\"9876543210\",\"designation\":\"MASON\",\"dailyWageRate\":800}"
```
*(Copy the returned `id` as `YOUR_WORKER_ID`)*

### 4. Create Site
```powershell
curl.exe -X POST http://localhost:8080/api/sites -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"siteName\":\"Site A\",\"location\":\"Delhi\"}"
```
*(Copy the returned `id` as `YOUR_SITE_ID`)*

### 5. Clock In
```powershell
curl.exe -X POST http://localhost:8080/api/attendance/clock-in -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"workerId\":\"YOUR_WORKER_ID\",\"siteId\":\"YOUR_SITE_ID\",\"clockInTime\":\"2025-05-01T07:00:00\"}"
```

### 6. Clock Out
```powershell
curl.exe -X POST http://localhost:8080/api/attendance/clock-out -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"workerId\":\"YOUR_WORKER_ID\",\"clockOutTime\":\"2025-05-01T18:00:00\"}"
```
*(Expect to see overtime calculated in the response)*

### 7. Overtime Summary
```powershell
curl.exe "http://localhost:8080/api/overtime/summary/YOUR_WORKER_ID?month=2025-05" -H "Authorization: Bearer YOUR_TOKEN"
```

### 8. Settle
```powershell
curl.exe -X POST "http://localhost:8080/api/overtime/settle/YOUR_WORKER_ID?month=2025-05" -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Ticket Verification

| Ticket | Test | Expected |
|---|---|---|
| LF-201 | CORS preflight | Origin-specific response, not `*` |
| LF-202 | App startup with Redis off | /active returns 200 with DB data |
| LF-203 | Pagination | PagedResponse meta, single DB query |
| LF-204 | SMS log | Event fires after settlement commit |
| LF-205 | Fast response | HTTP call outside txn; timeouts work |
