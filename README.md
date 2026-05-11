# AuthShield

AuthShield is a robust authentication and authorization service built with Spring Boot. This portfolio project demonstrates secure user management, JWT-based authentication, role-based access control, and integration with PostgreSQL and Redis for data persistence and caching.

## Features

- **User Authentication**: Secure login and registration with JWT tokens
- **Role-Based Access Control**: Manage user roles and permissions
- **JWT Token Management**: Access and refresh token handling with configurable expiration
- **User Management**: CRUD operations for users and roles
- **Swagger API Documentation**: Interactive API documentation with SpringDoc OpenAPI
- **Database Integration**: PostgreSQL for data storage with JPA/Hibernate
- **Caching**: Redis integration for performance optimization
- **Security Configuration**: Spring Security with custom filters and configurations
- **Docker Support**: Containerized deployment with Docker Compose

## Technologies Used

- **Java 17**
- **Spring Boot 4.0.5**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Redis**
- **JWT (JJWT)**
- **SpringDoc OpenAPI (Swagger)**
- **Maven**
- **Docker & Docker Compose**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL (optional, if not using Docker)
- Redis (optional, if not using Docker)

## Installation and Setup

### Option 1: Using Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd authshield
   ```

2. Build and run the application:
   ```bash
   docker-compose up --build
   ```

   This will start the application on `http://localhost:8000`, PostgreSQL on port 5432, and Redis on port 6379.

### Option 2: Local Development

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd authshield
   ```

2. Set up PostgreSQL and Redis locally, or update `application.properties` with your database credentials.

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

The application can be configured via `src/main/resources/application.properties`:

- **Server Port**: `server.port=8000`
- **Database**: PostgreSQL connection settings
- **JWT**: Secret key and token expiration times
- **Redis**: Host and port configuration
- **Logging**: Configurable logging levels

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh access token

### User Management
- `GET /api/v1/users` - Get all users (Admin only)
- `GET /api/v1/users/{id}` - Get user by ID (Admin only)
- `PUT /api/v1/users/{id}` - Update user (Admin only)
- `DELETE /api/v1/users/{id}` - Delete user (Admin only)

### Role Management
- `GET /api/v1/roles` - Get all roles (Authenticated users)
- `POST /api/v1/roles` - Create new role (Admin only)
- `PUT /api/v1/roles/{id}` - Update role (Admin only)
- `DELETE /api/v1/roles/{id}` - Delete role (Admin only)
- `POST /api/v1/roles/assign` - Assign role to user (Admin only)

## API Documentation

Access the Swagger UI for interactive API documentation at:
`http://localhost:8000/swagger-ui.html`

## Testing

Run the tests using Maven:
```bash
mvn test
```

### Postman
I verified each endpoint locally with Postman.

To add Postman coverage to the repository, export your collection and save it under `postman/AuthShield.postman_collection.json`.

## Project Structure

```
src/
├── main/
│   ├── java/com/authshield/authshield/
│   │   ├── AuthshieldApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── RoleController.java
│   │   │   └── UserController.java
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/authshield/authshield/
        └── AuthshieldApplicationTests.java
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Jaydeep Bariya - Software Developer

---

*This project showcases modern Java development practices, security implementation and microservices architecture principles.*