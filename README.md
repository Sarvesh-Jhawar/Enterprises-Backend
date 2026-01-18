# Enterprise SaaS Backend: Single Backend, Multiple Frontends

## 🚀 Overview
The **Enterprise SaaS Backend** is a high-performance, multi-tenant solution designed to power multiple independent frontends (businesses) from a single, unified backend instance. Built with **Spring Boot 3**, **JPA**, and **Supabase (PostgreSQL)**, it provides robust data isolation and business management tools for diverse enterprises.

---

## 🏗️ Architecture: The "Power of One"
This project implements a **Multi-tenant Architecture** where a single application instance serves multiple distinct customers (tenants). 

### How it Works: Path-Based Multi-tenancy
Instead of complex subdomains, this backend uses **Dynamic Path Resolution**. Every API request is prefixed with a `tenantSlug`:
`GET /api/{tenantSlug}/products`

1. **Tenant Identification**: The `TenantResolver` extracts the slug from the URL.
2. **Context Resolution**: The system verifies the tenant exists and is active.
3. **Data Isolation**: Database queries are automatically filtered by `tenant_id`, ensuring Business A never sees Business B's data.

### 🛡️ Security Features
- **Strict Tenant Isolation**: Admin users are bound to a specific `tenant_id`. Any attempt to access data from another tenant results in a `403 Forbidden` response.
- **Session-Based Authentication**: Secure admin logins using Spring Security and BCrypt password encoding.
- **Configurable CORS**: Dynamic origin management via the `ALLOWED_ORIGINS` environment variable, enabling multiple frontends (Vercel, Localhost, etc.) to securely connect.
- **Soft Deletion**: Products are never permanently deleted but marked as inactive, preserving historical data integrity.

---

## ❓ The Problem & Solution

### The Problem
Traditional business scaling often leads to:
- **Code Bloat**: Maintaining separate backend codebases for every new client.
- **High Infrastructure Costs**: Paying for multiple server instances (AWS/Railway).
- **Maintenance Nightmare**: Patching bugs across 10 different repositories simultaneously.

### What This Project Solves
- **Effortless Scaling**: Onboard a new business by simply adding a row to the `tenants` table. No new deployment required.
- **Centralized Management**: One source of truth for all business logic, updates, and security patches.
- **Resource Efficiency**: Multiple businesses share the same database connections and CPU/RAM resources, significantly lowering operational costs.
- **Uniform API Standards**: Every frontend consumes the same clean, documented API, ensuring consistency across the entire ecosystem.

---

## 🛠️ Technology Stack
- **Core**: Java 17, Spring Boot 3.x
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL (Supabase)
- **Security**: Spring Security 6 (Session-based)
- **Tooling**: Lombok, Maven, Docker

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Maven
- A PostgreSQL database (Supabase recommended)

### Environment Variables
Configure the following in your environment or `application.properties`:
- `DB_URL`: Your database JDBC URL.
- `DB_USERNAME`: Database user.
- `DB_PASSWORD`: Database password.
- `ALLOWED_ORIGINS`: Comma-separated list of allowed frontend URLs.

### Installation
```bash
# Clone the repository
git clone https://github.com/Sarvesh-Jhawar/Enterprises-Backend.git

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

---

## 📈 Future Roadmap
- [ ] JWT-based Stateless Authentication option.
- [ ] Tenant-specific branding/configuration JSON.
- [ ] Shared product inventory with cross-tenant sync.
- [ ] Multi-region database support.
