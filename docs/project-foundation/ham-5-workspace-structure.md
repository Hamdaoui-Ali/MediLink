# HAM-5 - Set Up Project Repository and Workspace Structure

## What This Issue Was About

This issue created the basic structure for the whole MediLink project. The goal was not to build features yet, but to make sure the repository is organized in a way that makes future work easy to find and easy to maintain.

For a non-technical reader, this is the equivalent of preparing the folders, labels, and house rules before moving furniture into a new office.

## What Was Added

- A clear root layout for the project.
- Separate top-level folders for backend code, frontend code, database notes, and documentation.
- A main `README.md` that explains what MediLink is and how to run it locally.
- A `.gitignore` file so temporary files, build outputs, and secrets are not committed.
- Shared workflow notes for branch naming and commit naming.

## Why It Matters

This work makes the project easier to understand for anyone joining later. It also reduces the chance of accidental mistakes, such as committing local environment secrets or mixing backend and frontend files in the same place.

It also establishes the basic solo-developer workflow for the project, so future work can be tracked consistently in Linear and in the repository history.

## Where To Look

- `README.md` - project summary and local setup notes
- `.gitignore` - files that should never be committed
- `docs/development-workflow.md` - branch and commit rules
- `database/README.md` - notes for future database scripts and seed data

## What Is Not Included Yet

- Real business features
- Actual database tables and migrations
- Authentication flows
- Booking, scheduling, or dashboards

This issue only prepared the workspace. It did not build the product logic yet.
