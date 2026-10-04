# Library Management System (Desktop App)

A multi-tier Desktop Application for university library management built with Java Swing/AWT and MySQL.

---

## Architecture Overview (5-Tier Architecture)

The system follows a strict layered architectural pattern to support separation of concerns and team-based parallel development:

```
src/
├── db/            # Database scripts, connection manager, configuration
├── model/         # Domain entities and Data Transfer Objects (DTOs)
├── repository/    # Data Access Layer using direct JDBC SQL execution
├── service/       # Business logic layer and global session control
├── controller/    # Mediators between presentation and business logic
├── ui/            # Java Swing GUI views and forms
└── MainApp.java   # Application entry point and pre-flight checks
```

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