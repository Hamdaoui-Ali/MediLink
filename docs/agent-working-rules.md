# Agent Working Rules For MediLink

This document defines the repo-specific rules the assistant should check before working on MediLink issues.

## Before Starting Any Issue

- Read the Linear issue details before changing files.
- Read the relevant Notion specification sections and treat Notion as the product source of truth.
- Inspect the current repository state before deciding how to implement the issue.
- Create and switch to a dedicated branch for the issue before making code or documentation changes.
- Check existing patterns in backend, frontend, database, and docs before adding new structure.
- Avoid unrelated refactors, formatting churn, or changes outside the issue scope.
- Preserve user-made changes and never revert files unless explicitly asked.

## Branch Rules

- Use one dedicated branch per Linear issue.
- Branch names should include the issue key and a short slug, for example `feature/ham-14-patient-registration-page`.
- Do not work on multiple Linear issues in the same branch unless the user explicitly asks for a combined branch.
- Do not merge issue branches automatically.
- Leave each issue branch open for the user to review and merge manually after they mark the issue done.
- If the repository is already on a branch for a different issue, create or switch to the correct issue branch before editing files.

## Linear Status Rules

- When the user asks the assistant to start work on a Linear issue, move that issue to `In Progress`.
- While actively checking or validating finished work, use `In Review` if that status better matches the current step.
- When implementation is finished, do not mark the issue `Done` immediately.
- Ask the user for approval before marking an issue `Done`.
- Mark the issue `Done` only after the user approves the completed work.
- If work is blocked by a missing decision, missing credentials, unavailable tooling, or an external dependency, leave the issue out of `Done` and explain the blocker.

## Implementation Rules

- Match the stack defined by the Notion specification and current project setup:
  - Backend: Java 21 with Spring Boot.
  - Frontend: Angular.
  - Database: MySQL.
  - Architecture: modular monolith with domain-driven boundaries.
- Keep backend code organized by domain packages where possible.
- Keep frontend code organized by feature areas and shared utilities.
- Keep database changes in `database/` or the backend migration folder, depending on the issue.
- Whenever a new function is added, add focused unit tests for that function in the same change unless the function is purely declarative configuration or a test is technically impossible; if skipped, document the reason.
- For schema design, document decisions for non-developer readers.
- For migrations and seed data, make the execution path clear and reproducible.

## Testing Rules

- Unit tests are required for new functions, but unit tests alone are not enough to finish an issue that changes user-visible behavior, API behavior, database behavior, authentication, authorization, or multi-step workflows.
- For every issue, add or update realistic tests that exercise the actual behavior through the closest practical boundary:
  - backend API workflows should have integration tests that start the Spring context, run migrations when relevant, call controllers through HTTP or MockMvc, and verify persisted data or returned responses;
  - frontend workflows should have component or service tests that verify the real form submission, navigation, error states, and API contract handling;
  - cross-feature flows should be tested together when the user experience depends on multiple features, for example register a patient and then log in with the same credentials;
  - database or migration issues should verify that the app can start against the intended local database profile and that required seed data exists.
- Tests must cover the success path, the main failure paths, and at least one realistic end-to-end or integration path for the issue when technically possible.
- Do not rely only on mocked unit tests for behavior that depends on database state, framework configuration, security configuration, serialization, routing, HTTP status codes, CORS, JWT generation, or frontend/backend API contracts.
- If a realistic integration or flow test cannot be added, document the exact limitation and perform a manual verification command or API call that proves the behavior.
- Before calling an issue ready for review, explain which tests are unit-level and which tests exercise the real behavior.

## Documentation Rules

- Add or update documentation when an issue changes setup, architecture, schema, workflow, or user-facing behavior.
- After finishing work on any Linear issue, create or update a well-structured documentation file under `docs/`.
- The issue documentation must explain what was done in clear language that non-developers can understand.
- The issue documentation must also include technical explanations for developers who need implementation detail.
- Explain the reasoning behind important choices, not only the final result.
- Include what changed, why it changed, where it lives in the repository, how it should be used, and what remains out of scope.
- Write documentation in plain language so non-developers can understand what changed and why it matters.
- Mention what is intentionally not included yet when the issue is foundational or partial.
- Keep issue-specific docs under `docs/` unless the repo already has a more specific location.

## Verification Rules

- Run the relevant local verification command before considering work complete.
- Run the relevant unit test command for newly added functions, or clearly document why those tests could not be run.
- Run the relevant integration or workflow tests for the actual issue behavior, not only isolated unit tests.
- Backend changes should usually be checked with `cmd /c mvnw.cmd test` from `medilink-backend`.
- Frontend changes should usually be checked with `npm.cmd run build` from `medilink-frontend`.
- Frontend behavior changes should usually also be checked with `npm.cmd test -- --watch=false` from `medilink-frontend`.
- Database-only design changes should be reviewed for SQL consistency and documented decisions.
- If a verification command cannot run, record the exact reason and what remains unverified.

## Communication Rules

- Before editing files, explain briefly what will be changed.
- After finishing, summarize:
  - what changed
  - where it changed
  - what verification was run
  - whether the Linear issue is ready for user approval
- Ask for approval before moving a Linear issue to `Done`.
