# Library Management System (Desktop App)

A multi-tier Desktop Application for university library management built with Java Swing/AWT and MySQL.

---

## Architecture Overview (Closed 4-Layer Architecture with MVC)

The system adheres strictly to the **Closed 4-Layer Architecture** integrated with the **MVC Pattern** in the Presentation layer, as specified in the Software Engineering course assignment (C6-D1) and SRS Section 5.1:

```
[Presentation Layer]     ui/ (View - Swing Forms)  <--->  controller/ (Controller)
                                      ↓
[Business Logic Layer]    service/ (Business rules, Transaction Management, Session)
                                      ↓
[Data Access Layer]       repository/ (Pure JDBC SQL execution via PreparedStatement)
                                      ↓
[Database Layer]          MySQL Database (quan_li_thu_vien schema via src/db/)

[Shared Domain Objects]   model/ (Domain Entities & Data Transfer Objects - DTOs)
[Application Bootstrap]   MainApp.java (Entry point, pre-flight checks, look & feel)
```

**Architectural Rules:**
- **Closed Architecture**: Each layer communicates strictly with its immediately adjacent layer below. Direct bypasses (e.g. `ui/` directly invoking `repository/`) and reverse calls are strictly forbidden.
- **MVC in Presentation**: Views (`ui/`) and Domain Models do not interact directly; all user interactions and navigation are orchestrated through the `controller/` layer.
- **Standards & Guidelines**: See [CODING_STANDARDS.md](CODING_STANDARDS.md) and [PROJECT_PLAN.md](PROJECT_PLAN.md) for naming conventions, traceability comment formats, and branch policies.

---

## Core Infrastructure (Person 5 Module)

### 1. Database Connection Management (`db/DBConnection.java`)
- **Singleton Pattern**: Ensures a single active connection instance across the desktop lifecycle to prevent connection leaks.
- **External Configuration**: Credentials and connection parameters loaded dynamically from `db.properties`.
- **Fault Tolerance**: Automatic reconnection upon state disruption, validation via `testConnection(int timeoutSeconds)`.

### 2. In-Memory Session Management (`service/SessionManager.java`)
- **Singleton Pattern**: Maintains the currently authenticated principal within local memory.
- **Role-Based Access Control (RBAC)**: Supports roles `QUAN_LI` (Manager), `THU_THU` (Librarian), and `SINH_VIEN` (Student).
- **Context Injection**: Provides operator IDs for audit fields (such as `ma_nhan_vien` on borrow receipts and invoices) without requiring repeated input.

### 3. Application Launcher (`MainApp.java`)
- **Look & Feel**: Configures system Look & Feel with antialiased font rendering.
- **Pre-flight Health Check**: Tests MySQL connectivity before presenting the UI; prompts with troubleshooting instructions if the database is offline.
- **Modular Entry Point**: Boots `ui.LoginForm` via reflection if compiled, or presents the built-in Infrastructure Diagnostic Dashboard.

---

## Database Setup

1. Start your local MySQL Server (via XAMPP or native service).
2. Execute the setup script:
   ```sql
   mysql -u root -p < src/db/setup_database.sql
   ```
3. Update database credentials in `db.properties` if not using the default `root` with no password:
   ```properties
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://localhost:3306/quan_li_thu_vien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
   db.user=root
   db.password=your_password
   ```

---

## Dependencies
- **JDK 17+**
- **MySQL Connector/J 8.3.0** (bundled in `lib/mysql-connector-j-8.3.0.jar`)