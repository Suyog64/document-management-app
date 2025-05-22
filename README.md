# Document Management System API

A comprehensive Spring Boot REST API for managing documents with search capabilities, access control, and a question-answering system.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [API Documentation](#api-documentation)
  - [Authentication](#authentication)
  - [User Management](#user-management)
  - [Document Management](#document-management)
  - [Q&A System](#qa-system)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## Overview

This Document Management System (DMS) provides a secure, scalable REST API for storing, retrieving, and searching documents. It features user authentication, role-based access control, full-text search capabilities, and a question-answering system to help users find relevant information within documents.

## Features

- **Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (Admin, Editor, Viewer roles)
  - Secure password storage with encryption

- **User Management**
  - User registration and authentication
  - Profile management
  - Role assignment and management

- **Document Management**
  - Document upload with metadata
  - Document retrieval and search
  - Document versioning
  - Support for various file types

- **Search & Q&A**
  - Full-text search capabilities
  - Keyword-based document retrieval
  - Question answering from document content
  - Relevant snippet extraction

- **Performance & Scalability**
  - Asynchronous processing with CompletableFuture
  - Pagination and sorting for large result sets
  - Efficient document indexing

## Technology Stack

- **Backend**: Java 11, Spring Boot 2.7
- **Security**: Spring Security, JWT
- **Database**: H2 Databse, PostgreSQL (configurable)
- **Build Tool**: Maven
- **Documentation**: Swagger/OpenAPI
- **Logging**: SLF4J, Logback

## API Documentation

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register a new user | Public |
| POST | `/api/auth/login` | Authenticate user and get JWT | Public |
| POST | `/api/auth/logout` | Logout user (client-side) | Authenticated |

#### Sample Requests

**Register User**
```json
POST /api/auth/register
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "securepassword123",
  "roles": ["editor", "viewer"]
}
```

**Login**
```json
POST /api/auth/login
{
  "username": "johndoe",
  "password": "securepassword123"
}
```

**Login Response**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "roles": ["ROLE_EDITOR", "ROLE_VIEWER"]
}
```

### User Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users` | Get all users | Admin |
| GET | `/api/users/{id}` | Get user by ID | Admin or Self |
| GET | `/api/users/profile` | Get current user profile | Authenticated |
| DELETE | `/api/users/{id}` | Delete user | Admin |
| PUT | `/api/users/{id}/role` | Update user roles | Admin |

#### Sample Requests

**Update User Role**
```json
PUT /api/users/1/role
["admin", "editor"]
```

### Document Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/documents/upload` | Upload new document | Editor, Admin |
| GET | `/api/documents/{id}` | Get document by ID | Viewer, Editor, Admin |
| GET | `/api/documents` | Get all documents | Viewer, Editor, Admin |
| GET | `/api/documents/user` | Get current user's documents | Viewer, Editor, Admin |
| POST | `/api/documents/search` | Search documents by criteria | Viewer, Editor, Admin |
| GET | `/api/documents/search` | Search documents by keyword | Viewer, Editor, Admin |
| PUT | `/api/documents/{id}` | Update document | Editor, Admin |
| DELETE | `/api/documents/{id}` | Delete document | Admin |

#### Sample Requests

**Upload Document**  
`POST /api/documents/upload` (Multipart Form Data)

Form fields:
- `metadata`: JSON object 
- `file`: Binary file data

Metadata JSON:
```json
{
  "title": "Annual Report 2024",
  "description": "Financial and operational results for fiscal year 2024",
  "tags": ["financial", "annual", "report"]
}
```

**Search Documents**
```json
POST /api/documents/search
{
  "title": "Annual Report",
  "fileType": "pdf",
  "startDate": "2023-01-01T00:00:00",
  "endDate": "2023-12-31T23:59:59",
  "authorId": 1,
  "keyword": "financial"
}
```

**Update Document**
```json
PUT /api/documents/1
{
  "title": "Updated Annual Report 2024",
  "description": "Updated financial and operational results for fiscal year 2024",
  "tags": ["financial", "annual", "report", "updated"]
}
```

### Q&A System

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/qa/question` | Ask a question | Viewer, Editor, Admin |
| GET | `/api/qa/recent` | Get recent documents | Viewer, Editor, Admin |
| GET | `/api/qa/popular-terms` | Get popular search terms | Viewer, Editor, Admin |

#### Sample Requests

**Ask Question**
```json
POST /api/qa/question
{
  "question": "What are the financial results for Q1 2024?",
  "context": "I'm looking for information about the first quarter financial performance"
}
```

**Ask Question Response**
```json
{
  "question": "What are the financial results for Q1 2024?",
  "snippets": [
    {
      "documentId": 5,
      "title": "Q1 2024 Financial Report",
      "textSnippet": "...Revenue increased by 15% to $24.3 million in Q1 2024, compared to $21.1 million in the same period last year. Operating expenses were $18.7 million...",
      "author": "janedoe",
      "createdAt": "2024-04-15T10:30:45"
    },
    {
      "documentId": 8,
      "title": "Executive Summary - Q1 2024",
      "textSnippet": "...The first quarter of 2024 showed strong performance across all business units with a total revenue of $24.3 million and EBITDA of $5.6 million...",
      "author": "robertsmith",
      "createdAt": "2024-04-12T14:20:30"
    }
  ],
  "totalResults": 2
}
```

## Getting Started

### Prerequisites

- JDK 11 or higher
- Maven 3.6+
- PostgreSQL (or other supported database)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/document-management-system.git
   cd document-management-system
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:document_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT
app.jwtSecret=yourSecretKey
app.jwtExpirationMs=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Usage Examples

### Authentication Flow

1. Register a new user
2. Login to get a JWT token
3. Include the token in subsequent requests:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### Document Upload and Search

1. Upload a document with metadata
2. Search for documents using keywords
3. Retrieve a specific document by ID
4. Ask questions related to document content

## Testing

Recommended API testing sequence:

1. **Authentication Setup**
   - Register test accounts
   - Login to obtain JWT tokens

2. **User Management**
   - Verify user creation
   - Test role management

3. **Document Operations**
   - Upload test documents
   - Verify document retrieval

4. **Search & Q&A Functionality**
   - Test document search
   - Test Q&A functionality

5. **Update & Delete Operations**
   - Test document updates
   - Test document deletion

## Security

- All endpoints except for registration and login require authentication
- Role-based access control limits actions based on user roles
- Passwords are encrypted using BCrypt
- JWT tokens are used for stateless authentication
- HTTPS is strongly recommended for production

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
