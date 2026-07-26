# Problem
Need to build a customer support management system
for my business where customer emails
are managed in a better way

# Solution
Customer sends email to my customer support email address
Email is converted to ticket in my app we build
Email is auto categorized using AI
Email is auto responded by AI agent using knowledge base
If email is not responded a human logs in using emails

# MVP Feature Plan

## Email & Tickets
- Inbound customer email arrives via webhook and becomes a ticket.
- Replies from the customer attach to the same ticket (threaded), not a new one.
- Ticket thread is viewable in the UI like a conversation.

## Ticket Statuses
- New
- AI Responded
- Needs Review (AI drafted, low confidence, admin must check)
- Escalated (AI couldn't help, admin must write reply)
- Pending Customer
- Resolved
- Closed

## AI Handling
- AI assigns each ticket a category from an admin-defined fixed list.
- AI drafts a reply using the knowledge base.
- AI draft requires admin approval before sending (no auto-send in MVP).
- No confident KB match → ticket goes to Escalated instead of a draft.

## Knowledge Base
- Admin uploads PDF/Word docs as the knowledge base — no article editor, no separate content format.
- AI reads/references these docs directly to draft replies. Kept simple: no versioning, folders, or tagging in MVP.

## Admin Access
- Single admin user, login-protected dashboard.
- Customers have no login — email only, no portal.

## Dashboard / Metrics
- Average response time (overall, and AI vs. human split)
- Total tickets per day (volume chart)
- Tickets by status (chart)
- Tickets by category (chart)
- Resolution rate

## Security
- Admin authentication (login-gated dashboard)
- Webhook signature verification
- No PII/full email bodies in plaintext logs

## Out of Scope for MVP
- Customer-facing portal/login
- Multiple admins, roles/permissions
- Auto-send of AI replies without admin approval
- SLA tracking/alerts
- Auto-improving AI from admin corrections (feedback loop)
- Multi-channel support (chat, SMS, social)
- Attachment scanning/preview
- Advanced reporting/export
- Multi-language support
- Tech stack / hosting / infra decisions
