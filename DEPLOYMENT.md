# Deployment Guide: Supabase + Render

Frontend is already live on Vercel. This guide covers deploying the backend (Spring Boot) to Render and the database (PostgreSQL) to Supabase.

---

## Overview

| Layer | Service | Status |
|---|---|---|
| Frontend | Vercel | Done |
| Backend | Render | To do |
| Database | Supabase | To do |

---

## Phase 1 — Code Changes

Do these before setting up anything on Render or Supabase.

### 1. Update `application.yml`

File: `canteen-system/src/main/resources/application.yml`

Replace the entire file content with:

```yaml
spring.datasource:
  driver-class-name: "org.postgresql.Driver"
  url: "${DB_URL}"
  username: "${DB_USERNAME}"
  password: "${DB_PASSWORD}"

spring.jpa:
  show-sql: false
  hibernate.ddl-auto: update
  properties:
    hibernate:
      jdbc:
        time_zone: Asia/Hong_Kong

server:
  port: ${PORT:8080}
```

**Why:** Removes hardcoded localhost credentials. Render dynamically assigns a port — the `server.port` line is required or the app won't receive traffic.

### 2. Update the API base URL in the frontend

File: `frontend-canteen/js/api.js`

Find the line with `http://localhost:8080` and replace it with your Render URL.

> You won't have the Render URL until Phase 3. Come back and do this step after Phase 3 is done.

### 3. Commit and push both changes to GitHub

```bash
git add .
git commit -m "configure env vars and port for production deployment"
git push
```

---

## Phase 2 — Supabase (Database)

### Step 1: Create a project

1. Go to [supabase.com](https://supabase.com) and sign up / log in
2. Click **New project**
3. Fill in:
   - **Project name**: `canteen-db` (or anything you like)
   - **Database password**: set a strong password — save it, you'll need it later
   - **Region**: pick one closest to you
4. Click **Create new project** and wait ~2 minutes for it to provision

### Step 2: Get the connection string

1. In your project, go to **Settings** (gear icon, bottom-left) → **Database**
2. Scroll down to the **Connection string** section
3. Select the **URI** tab
4. Copy the URI — it looks like:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxxxxxxxx.supabase.co:5432/postgres
   ```
5. Replace `[YOUR-PASSWORD]` with the password you set in Step 1

### Step 3: Convert to JDBC format

Add `jdbc:` to the front of the URI:

```
jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
```

Save this string — you'll paste it into Render as the `DB_URL` environment variable.

> **No need to create tables manually.** The app uses `hibernate.ddl-auto: update` so Hibernate will auto-create all tables on first startup.

---

## Phase 3 — Render (Backend)

### Step 1: Create a Web Service

1. Go to [render.com](https://render.com) and sign up / log in
2. Click **New +** → **Web Service**
3. Connect your GitHub account and select the `canteen-management-system` repository

### Step 2: Configure the service

| Setting | Value |
|---|---|
| Name | `canteen-backend` |
| Root Directory | `canteen-system` |
| Environment | `Java` |
| Build Command | `mvn clean package -DskipTests` |
| Start Command | `java -jar target/canteen-system-0.0.1-SNAPSHOT.jar` |
| Instance Type | Free |

### Step 3: Add environment variables

Scroll down to the **Environment Variables** section and add:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | your Supabase database password |

### Step 4: Deploy

1. Click **Create Web Service**
2. Render will build and deploy your JAR — first build takes 3–5 minutes
3. Once done, your backend URL appears at the top of the page:
   ```
   https://canteen-backend-xxxx.onrender.com
   ```
   Copy this URL.

---

## Phase 4 — Connect Frontend to Backend

1. Open `frontend-canteen/js/api.js`
2. Find the line with `http://localhost:8080` and replace it with your Render URL:
   ```
   https://canteen-backend-xxxx.onrender.com
   ```
3. Commit and push — Vercel will auto-redeploy the frontend

---

## Verify Everything Works

1. Open your Vercel frontend URL
2. Log in with the default seed accounts:

   | Role | School ID | Password |
   |---|---|---|
   | Admin | `ADMIN001` | `admin123` |
   | Student | `STU001` | `student123` |
   | Kitchen | `KITCHEN001` | `kitchen123` |

3. If login works, the frontend → backend → database chain is connected

---

## Known Limitations (Free Tiers)

| Issue | Cause | Fix |
|---|---|---|
| First request after idle takes ~30s | Render free tier spins down after 15 min of inactivity | Upgrade to a paid Render instance for always-on |
| Database becomes inaccessible after a week | Supabase pauses inactive free-tier projects | Log in to Supabase dashboard and click **Restore** |

---

## Troubleshooting

**Build fails on Render**
- Check that **Root Directory** is set to `canteen-system`, not the repo root
- Check the build logs for Java/Maven errors

**App starts but crashes immediately**
- Go to Render → your service → **Logs** tab
- Most likely cause: wrong `DB_URL` format or wrong password
- Double-check the JDBC URL starts with `jdbc:postgresql://`

**Login returns 401 or network error**
- Open browser DevTools → Network tab → check what URL the request is going to
- If it's still hitting `localhost:8080`, the `api.js` base URL was not updated

**Tables not created in Supabase**
- Go to Supabase → **Table Editor** — tables appear after the first successful app startup
- If missing, check Render logs for Hibernate errors
