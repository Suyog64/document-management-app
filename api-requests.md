# Document Management System API Request Examples

This file contains sample JSON request bodies for all the APIs in the document management system. The examples are organized by controller.

## Authentication Controller

### Login
**POST** `/api/auth/login`
```json
{
  "username": "johndoe",
  "password": "securepassword123"
}
```

### Register
**POST** `/api/auth/register`
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "securepassword123",
  "roles": ["editor", "viewer"]
}
```

### Logout
**POST** `/api/auth/logout`
```json
{}
```

## User Controller

### Get All Users
**GET** `/api/users`
(No request body required)

### Get User by ID
**GET** `/api/users/{id}`
(No request body required)

### Get Current User Profile
**GET** `/api/users/profile`
(No request body required)

### Delete User
**DELETE** `/api/users/{id}`
(No request body required)

### Update User Role
**PUT** `/api/users/{id}/role`
```json
["admin", "editor"]
```

## Document Controller

### Upload Document
**POST** `/api/documents/upload` (Multipart Form Data)

Form fields:
- `metadata`: JSON object (see below)
- `file`: Binary file data

Metadata JSON:
```json
{
  "title": "Annual Report 2024",
  "description": "Financial and operational results for fiscal year 2024",
  "tags": ["financial", "annual", "report"]
}
```

### Get Document by ID
**GET** `/api/documents/{id}`
(No request body required)

### Get All Documents
**GET** `/api/documents`
(No request body required, uses query parameters for pagination)

Example query parameters:
- `page=0`
- `size=10`
- `sortBy=createdAt`
- `sortDir=desc`

### Get Current User's Documents
**GET** `/api/documents/user`
(No request body required, uses query parameters for pagination)

### Search Documents (POST)
**POST** `/api/documents/search`
```json
{
  "title": "Annual Report",
  "fileType": "pdf",
  "startDate": "2023-01-01T00:00:00",
  "endDate": "2023-12-31T23:59:59",
  "authorId": 1,
  "keyword": "financial"
}
```

### Search Documents by Keyword (GET)
**GET** `/api/documents/search?keyword=annual&page=0&size=10`
(No request body required, uses query parameters)

### Update Document
**PUT** `/api/documents/{id}`
```json
{
  "title": "Updated Annual Report 2024",
  "description": "Updated financial and operational results for fiscal year 2024",
  "tags": ["financial", "annual", "report", "updated"]
}
```

### Delete Document
**DELETE** `/api/documents/{id}`
(No request body required)

### Get Unprocessed Documents
**GET** `/api/documents/unprocessed`
(No request body required)

## Q&A Controller

### Ask Question
**POST** `/api/qa/question`
```json
{
  "question": "What are the financial results for Q1 2024?",
  "context": "I'm looking for information about the first quarter financial performance"
}
```

### Get Recent Documents
**GET** `/api/qa/recent?page=0&size=10`
(No request body required, uses query parameters)

### Get Popular Terms
**GET** `/api/qa/popular-terms`
(No request body required)

## Authentication Headers

For endpoints requiring authentication, include the following header:

```
Authorization: Bearer {jwt_token}
```

Where `{jwt_token}` is the token received from the login response.
