# API Contract

This document defines the API interface between Hive AI and its backend.
It is the single source of truth for endpoints, DTOs, error handling, and upload constraints.

## Base URL

`http://172.16.50.91:5019/` — local network, cleartext HTTP

## Endpoints

### Authentication

| Method | Path | Request | Response | Auth |
|--------|------|---------|----------|------|
| POST | api/Auth/login | LoginRequest | LoginResponse | No |

### Campaigns

| Method | Path | Request | Response | Auth |
|--------|------|---------|----------|------|
| GET | api/Campaigns | — | List<CampaignResponse> | Yes |
| GET | api/Campaigns/{eventId} | — | CampaignResponse | Yes |
| PUT | api/Campaigns/{eventId}/approve | — | Unit | Yes (Reviewer) |
| PUT | api/Campaigns/{eventId}/reject | ApprovalRequest | Unit | Yes (Reviewer) |
| PUT | api/Campaigns/{eventId} | EditCampaignRequest | Unit | Yes (Designer) |
| POST | api/Campaigns/{eventId}/image | MultipartBody | UploadImageResponse | Yes (Designer) |

### Events

| Method | Path | Request | Response | Auth |
|--------|------|---------|----------|------|
| GET | api/Events | — | List<EventResponse> | Yes |
| PUT | api/Events/{eventId}/cancel | — | Unit | Yes |
| PUT | api/Events/{eventId}/image | MultipartBody | UploadImageResponse | Yes (Designer) |

### Admin

| Method | Path | Request | Response | Auth |
|--------|------|---------|----------|------|
| GET | api/Admin/users | — | List<UserResponse> | Yes (Admin) |
| POST | api/Admin/users | CreateUserRequest | Unit | Yes (Admin) |
| PUT | api/Admin/users/{id}/activate | — | Unit | Yes (Admin) |
| PUT | api/Admin/users/{id}/deactivate | — | Unit | Yes (Admin) |

### Audit

| Method | Path | Request | Response | Auth |
|--------|------|---------|----------|------|
| GET | api/Admin/audit-logs | — | List<AuditLogResponse> | Yes (Admin) |

## Request DTOs

| DTO | Fields |
|-----|--------|
| LoginRequest | email, password |
| CreateUserRequest | name, email, password, role |
| ApprovalRequest | eventId, comments |
| EditCampaignRequest | campaignPost, hashtags |

## Response DTOs

| DTO | Fields |
|-----|--------|
| LoginResponse | message, token, userId, name, role |
| CampaignResponse | campaignId, eventId, campaignPost, hashtags, cta, imagePrompt, imageUrl, status, createdAt |
| EventResponse | id, title, description, startTime, endTime, location, eventType, status, approvalDeadline, designerImageUrl |
| UserResponse | id, name, email, passwordHash (@Transient), role, isActive, createdAt |
| AuditLogResponse | id, userId, action, details, createdAt |
| UploadImageResponse | message, imageUrl |

## Error Codes

| Code | AuthError Type | Message |
|------|---------------|---------|
| 401 | TokenExpired | Session expired. Please log in again. |
| 401 (login) | InvalidCredentials | Invalid email or password. |
| 403 | Unauthorized | You don't have permission to perform this action. |
| Other | Unknown | Request failed (code) |

## File Upload Constraints

- MIME types: PNG, JPEG only
- Maximum size: 10MB
- Encoding: MultipartBody.Part
- Temp file cleanup: `finally` block ensures disk space is freed
- Client-side validation before network transfer

## Unimplemented Endpoints

These backend endpoints exist but are not consumed by the frontend:

- GET /api/Approval/history
- GET /api/Approval/pending
- POST /api/Events
- GET /api/Events/today
- POST /api/Events/sync

How endpoints are consumed → `architecture.md`
How to add new endpoints → `playbook.md`
