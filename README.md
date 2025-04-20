# Leave Management System Backend

This is a Spring Boot backend for a Leave Management System, built for Africa HR to streamline employee leave applications and management in compliance with the Rwandan Labor Law (2023). The system supports various leave types (PTO, Sick, Maternity, Compassionate, etc.), Microsoft Azure AD authentication, local file storage for profile pictures, and a robust API for frontend integration.

## Table of Contents

 1. Features
 2. Tech Stack
 3. Prerequisites
 4. Setup Instructions
    - Environment Variables
    - Database Setup
    - Azure AD Setup
    - File Storage Setup
    - Building and Running Locally
    - Docker Deployment
 5. API Documentation
    - Authentication
    - Endpoints
      - Employee Endpoints
      - Manager Endpoints
      - Profile Picture Endpoint
    - Error Responses
 6. Data Models
 7. Security
 8. Production Considerations
 9. Testing
10. Troubleshooting
11. Submission Instructions

## Features

- **Employee Dashboard**: View leave balances, submit leave requests, check leave history, and upload documents.
- **Leave Application**: Apply for various leave types (full-day or half-day) with optional reasons and documents.
- **Approval Workflow**: Managers can approve/reject leave requests with comments.
- **Leave Balance Management**: Auto-accrues PTO (1.66 days/month), supports carry-forward (max 5 days).
- **Authentication**: Integrates with Microsoft Azure AD, restricts logins to `@ist.com` emails in production, and fetches profile pictures.
- **Notifications**: Sends email alerts for leave submissions, approvals, and rejections.
- **File Storage**: Stores profile pictures locally with URLs in the database.
- **Roles**: Supports `STAFF`, `MANAGER`, and `ADMIN` roles with role-based access control.

## Tech Stack

- **Backend**: Spring Boot 3.4.3 (Spring Web, Data JPA, Security, Mail)
- **Database**: MySQL
- **Authentication**: Microsoft Azure AD (OAuth 2.0)
- **File Storage**: Local file system for profile pictures
- **Dependencies**: Lombok, Microsoft Graph SDK, MySQL driver
- **Build Tool**: Maven
- **Containerization**: Docker

## Prerequisites

- **Java**: JDK 17
- **Maven**: 3.8.x or higher
- **MySQL**: 8.0.3x or higher
- **Docker**: For containerized deployment
- **Azure AD**: An Azure AD tenant for authentication
- **SMTP Server**: For email notifications (e.g., Gmail)
- **IDE**: IntelliJ IDEA, VS Code with Cursor/Windsurf, or similar (recommended for AI-driven development)

## Setup Instructions

### Environment Variables

Create a `.env` file or set environment variables for the following:

| Variable | Description | Example |
| --- | --- | --- |
| `DB_URL` | MySQL database URL | `jdbc:mysql://localhost:3306/leave_management` |
| `DB_USERNAME` | Database username | `username` |
| `DB_PASSWORD` | Database password | `password` |
| `MAIL_USERNAME` | SMTP server username | `your-email@gmail.com` |
| `MAIL_PASSWORD` | SMTP server password | `your-app-password` |
| `AZURE_CLIENT_ID` | Azure AD application client ID | `your-client-id` |
| `AZURE_CLIENT_SECRET` | Azure AD application client secret | `your-client-secret` |
| `AZURE_TENANT_ID` | Azure AD tenant ID | `your-tenant-id` |
| `PROFILE_PICTURES_DIR` | Local directory for profile pictures | `~/profile-pictures` |
| `APP_BASE_URL` | Base URL for the application | `http://localhost:8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (set to `prod` in production) | `dev` or `prod` |

### Database Setup

1. Install PostgreSQL and create a database named `leave_management`.

2. Update `application.properties` or environment variables with your database credentials.

3. The schema is managed by Spring Data JPA (`ddl-auto: update`).

### Azure AD Setup

1. Register an application in Azure AD:

   - Go to Azure Portal &gt; Azure Active Directory &gt; App registrations &gt; New registration.
   - Set redirect URI to `http://localhost:8080/login/oauth2/code/azure` (or your production URL).
   - Grant permissions: `User.Read`, `User.ReadBasic.All`, `openid`, `profile`, `email`.

2. Note the `client-id`, `client-secret`, and `tenant-id`.

3. Update `application.properties` or environment variables with these values.

4. Ensure the Microsoft Graph SDK is included in `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.microsoft.graph</groupId>
       <artifactId>microsoft-graph</artifactId>
   </dependency>
   ```

### File Storage Setup

1. Create a directory for profile pictures (e.g., `~/profile-pictures`).

2. Ensure the application has write permissions:

   ```bash
   mkdir -p ~/profile-pictures
   chmod 775 ~/profile-pictures
   ```

3. Set `file.storage.profile-pictures-dir` in `application.properties` or via `PROFILE_PICTURES_DIR`.

### Building and Running Locally

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd leave-management-system
   ```

2. Build the project:

   ```bash
   mvn clean package
   ```

3. Run the application:

   ```bash
   java -jar target/leave-management-system-0.0.1-SNAPSHOT.jar
   ```

4. Access the API at `http://localhost:8080`.

### Docker Deployment

1. Build the Docker image:

   ```bash
   docker build -t myrepo/leave-management-system:latest .
   ```
   
3. Push to Docker Hub:

   ```bash
   docker push myrepo/leave-management-system:latest
   ```

## API Documentation

### Authentication

The backend uses **OAuth 2.0 with Microsoft Azure AD**. Frontend clients must:

1. Redirect users to the Azure AD login page (`/oauth2/authorization/azure`).

2. Handle the callback at `/login/oauth2/code/azure`.

3. Include the OAuth 2.0 access token in the `Authorization` header for all API requests:

   ```
   Authorization: Bearer <access-token>
   ```

### Endpoints

All endpoints are prefixed with `/api` and require authentication unless specified. Responses are in JSON format.

#### Employee Endpoints

**1. Submit Leave Request**

- **URL**: `POST /api/employee/leave-request`

- **Roles**: STAFF, MANAGER, ADMIN

- **Description**: Submits a new leave request.

- **Request Body**:

  ```json
  {
    "userId": "Long",
    "leaveTypeId": "Long",
    "startDate": "YYYY-MM-DD",
    "endDate": "YYYY-MM-DD",
    "isHalfDay": boolean,
    "reason": "string" (optional),
    "documentUrl": "string" (optional)
  }
  ```

  - `userId`: Long id of the requesting user.
  - `leaveTypeId`: Long id of the leave type.
  - `startDate`, `endDate`: Must be present or future dates.
  - `isHalfDay`: True for half-day leave.
  - `reason`: Required if leave type requires it.
  - `documentUrl`: URL to a supporting document (required for some leave types).

- **Response** (200 OK):

  ```json
  {
    "id": "Long",
    "userId": "Long",
    "leaveTypeId": "Long",
    "startDate": "YYYY-MM-DD",
    "endDate": "YYYY-MM-DD",
    "isHalfDay": boolean,
    "reason": "string",
    "documentUrl": "string",
    "status": "PENDING",
    "approverId": null,
    "approverComments": null
  }
  ```

- **Errors**:

  - 400: Invalid input (e.g., missing reason, insufficient balance).
  - 401: Unauthorized (invalid or missing token).
  - 403: Forbidden (insufficient role).

**2. Get Leave Balance**

- **URL**: `GET /api/employee/leave-balance/{userId}/{leaveTypeId}/{year}`

- **Roles**: STAFF, MANAGER, ADMIN

- **Description**: Retrieves the leave balance for a user, leave type, and year.

- **Path Parameters**:

  - `userId`: Long of the user.
  - `leaveTypeId`: Long of the leave type.
  - `year`: Integer (e.g., 2025).

- **Response** (200 OK):

  ```json
  {
    "id": "Long",
    "userId": "Long",
    "leaveTypeId": "Long",
    "year": integer,
    "balance": number,
    "carriedForwardDays": number
  }
  ```

- **Errors**:

  - 401: Unauthorized.
  - 403: Forbidden.
  - 404: Balance not found (initializes a new balance if none exists).

#### Manager Endpoints

**1. Approve Leave Request**

- **URL**: `POST /api/manager/approve/{requestId}`

- **Roles**: MANAGER, ADMIN

- **Description**: Approves a pending leave request and deducts the balance.

- **Path Parameters**:

  - `requestId`: Long id of the leave request.

- **Query Parameters**:

  - `approverId`: Long id of the approving user (required).
  - `comments`: Approval comments (optional).

- **Response** (200 OK):

  ```json
  {
    "id": "Long",
    "userId": "Long",
    "leaveTypeId": "Long",
    "startDate": "YYYY-MM-DD",
    "endDate": "YYYY-MM-DD",
    "isHalfDay": boolean,
    "reason": "string",
    "documentUrl": "string",
    "status": "APPROVED",
    "approvedBy": "Long",
    "approvalComment": "string"
  }
  ```

- **Errors**:

  - 400: Invalid request (e.g., request not pending).
  - 401: Unauthorized.
  - 403: Forbidden.
  - 404: Request or approver not found.

## Data Models

The following JSON schemas represent the primary data models returned by the API.

**LeaveRequestDto**:

```json
{
  "id": "Long",
  "userId": "Long",
  "leaveTypeId": "Long",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "isHalfDay": boolean,
  "reason": "string",
  "documentUrl": "string",
  "status": "PENDING | APPROVED | REJECTED",
  "approvedBy": "Long",
  "approvalComment": "string"
}
```

**LeaveBalanceDto**:

```json
{
  "id": "Long",
  "userId": "Long",
  "leaveTypeId": "Long",
  "year": integer,
  "balance": number,
  "carriedForwardDays": number
}
```

**User** (not exposed directly but relevant for frontend):

```json
{
  "id": "Long",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "profilePictureUrl": "string",
  "roleType": "STAFF | MANAGER | ADMIN"
}
```

## Security

- **Authentication**: OAuth 2.0 with Azure AD. The frontend must handle the OAuth flow and include the access token in requests.
- **Authorization**: Role-based access control:
  - `STAFF`: Can submit leave requests and view balances.
  - `MANAGER`: Can approve/reject requests.
  - `ADMIN`: Full access (not fully implemented in provided code).
- **Email Restriction**: In production (`prod` profile), only `@ist.com` emails are allowed.
- **File Access**: Profile pictures are restricted to authenticated users.
- **HTTPS**: Use HTTPS in production to secure API calls.

## Production Considerations

- **File Storage**: Local file storage is not scalable. Consider AWS S3 for production:

  - Update `FileStorageService` to use the AWS SDK.

  - Add dependency:

    ```xml
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <version>2.20.0</version>
    </dependency>
    ```

- **Database**: Use a managed PostgreSQL service (e.g., AWS RDS).

- **Scaling**: Deploy on a cloud platform (e.g., AWS ECS, Kubernetes) with load balancing.

- **Monitoring**: Add Spring Boot Actuator for health checks and metrics:

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ```

- **Backup**: Regularly back up the `~/profile-pictures` directory and database.

- **Environment**: Set `SPRING_PROFILES_ACTIVE=prod` and configure production URLs in `application.yml`.
