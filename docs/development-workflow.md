# Development Workflow

## Branching

Use one task-focused branch per Linear issue.
Create and switch to that branch before changing code, tests, or documentation for the issue.

Examples:

- `feature/ham-6-backend-foundation`
- `feature/ham-7-angular-foundation`
- `chore/workspace-setup`

Do not merge issue branches automatically. The owner reviews and merges each issue branch manually after marking the issue done.

## Commits

Keep commits small and descriptive.

Examples:

- `feat: scaffold angular foundation`
- `chore: add workspace readme and gitignore`
- `refactor: align backend config with mysql target`

## Delivery Rule

Before marking a Linear issue done:

1. Confirm the acceptance criteria from the issue text.
2. Add or update focused unit tests for new functions.
3. Add or update realistic integration, workflow, or component tests for the actual user/API/database behavior.
4. Run the relevant local verification commands.
5. Check that setup notes exist for the next person opening the repo.

Do not treat mocked unit tests as enough when the issue depends on framework configuration, database migrations, HTTP contracts, JWT/security behavior, routing, or frontend/backend integration. In those cases, include a real behavior test or document a manual verification command that proves the flow works.
