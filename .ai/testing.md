# Testing

This document defines the testing strategy for Hive AI.
It is the single source of truth for manual testing, regression points, and future automation.

## Current State

- Placeholder tests only (ExampleUnitTest, ExampleInstrumentedTest)
- No real test coverage
- Manual testing is the primary verification method

## Manual Testing Checklist

### Login (All Roles)
- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials shows error
- [ ] Login with empty fields shows validation error
- [ ] Role-based routing works (Admin → AdminDashboard, Designer → DesignerDashboard, Reviewer → Dashboard)

### Admin Dashboard
- [ ] Stats display correctly (excluding admins from counts)
- [ ] User list loads and displays
- [ ] Activate user works
- [ ] Deactivate user works
- [ ] Create user dialog opens and works
- [ ] Role dropdown offers Reviewer, Designer, Admin
- [ ] Audit logs load and display
- [ ] Logout works and redirects to login

### Designer Dashboard
- [ ] Events load (excluding Started, Cancelled, with-campaign)
- [ ] Campaigns load (Generated status only)
- [ ] Poster upload works (valid image)
- [ ] Poster upload rejects invalid file type
- [ ] Poster upload rejects oversized file
- [ ] Campaign edit works (Generated status only)
- [ ] Campaign edit locked for Approved/Rejected/Posted
- [ ] Logout works

### User Dashboard
- [ ] Stat cards display correctly
- [ ] Campaign queue loads
- [ ] Upcoming events load
- [ ] Campaign detail navigation works
- [ ] Campaign approval works
- [ ] Campaign rejection works
- [ ] Event list loads
- [ ] Event cancel works
- [ ] Notifications load
- [ ] Logout works

### Edge Cases
- [ ] Empty states display correctly (no campaigns, no events, no notifications)
- [ ] Error states display correctly (network failure)
- [ ] Loading states display correctly (spinners appear)
- [ ] Back navigation works correctly
- [ ] Bottom navigation works correctly

## Regression Test Points

- Authentication flow (login, logout, session expiry)
- Role-based routing (Admin, Designer, Reviewer)
- Campaign approval workflow (Generated → Approved/Rejected → Posted)
- File upload validation (MIME type, size)
- Navigation back stack

## Future Automated Testing

### Unit Tests
- ViewModel state transitions
- Repository error handling
- DateUtils formatting
- StatusUtils color mapping

### UI Tests
- Login flow
- Campaign approval flow
- Navigation between screens

### Integration Tests
- Network layer with mock server
- End-to-end campaign workflow
