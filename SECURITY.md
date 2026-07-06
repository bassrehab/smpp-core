# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

The latest 1.0.x release receives security fixes. Older minor lines are not
maintained once a newer one is available.

## Reporting a Vulnerability

Please do not report security vulnerabilities through public GitHub issues.

Instead, use one of these channels:

- **GitHub private vulnerability reporting** (preferred):
  [Report a vulnerability](https://github.com/bassrehab/smpp-core/security/advisories/new)
- **Email**: contact@smppgateway.io with subject line `[SECURITY] smpp-core`

Include as much of the following as you can: affected version, a description
of the issue and its impact, and steps or a proof-of-concept to reproduce it.

## What to Expect

- **Acknowledgement** of your report within 72 hours.
- **Assessment and triage** within 7 days, including a severity estimate.
- **A fix or mitigation plan** for confirmed vulnerabilities, targeted at the
  next patch release. Credit is given to reporters unless anonymity is
  requested.

Please allow us reasonable time to release a fix before public disclosure.

## Scope

smpp-core parses untrusted network input (SMPP PDUs). Malformed-PDU handling,
decoder resource exhaustion, and TLS configuration issues are all in scope
and taken seriously.
