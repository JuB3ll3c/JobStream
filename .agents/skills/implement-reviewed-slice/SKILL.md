---
name: implement-reviewed-slice
description: Implement one explicitly approved slice with TDD, targeted tests, and a two-axis review of the uncommitted changes. Stop before any commit or follow-up slice.
---

# Implement a Reviewed Slice

Implement exactly one small, explicitly user-approved slice, review its uncommitted changes, and then stop for validation.

## 1. Approval checkpoint

Inspect the request, relevant specification, repository instructions, and current working tree. Preserve all pre-existing changes.

Before modifying any file:

- Restate the intended outcome and scope.
- List the files expected to change, including generated artifacts.
- State what remains outside scope.
- Identify the test seams and targeted test commands.
- Wait for explicit user approval.

If implementation later requires additional files or broader scope, stop and request renewed approval. Also stop before adding a dependency, introducing a database migration or schema change, making a material architectural change, or departing from existing project patterns.

## 2. TDD implementation

Use `/tdd` at the approved seams where possible. Work through one red-green slice at a time:

1. Write one behavioral test for the approved behavior.
2. Run it and confirm that it fails for the expected reason.
3. Add only enough implementation to make it pass.
4. Run the targeted test again.

Do not anticipate later slices, write tests for future behavior, or perform unrelated refactoring. Run relevant typechecking or build checks for the files changed.

## 3. Review the uncommitted slice

Never create a commit.

Prepare a complete review bundle containing only changes made for the approved slice. Include tracked and staged diffs plus the full content of new untracked files. Exclude unrelated pre-existing changes.

Launch two read-only review sub-agents in parallel:

- **Standards review:** check correctness, regressions, test quality, security concerns, and compliance with repository instructions and established project patterns.
- **Scope review:** compare the changes with the approved slice; identify missing behavior, incorrect behavior, scope creep, future-slice work, and unrelated refactoring.

Give both reviewers the approved scope, out-of-scope statement, changed-file list, review bundle, and relevant tests. Reviewers must not modify files.

## 4. Report and stop

Present the two reviews separately under `## Standards` and `## Scope`. Then report:

- Files changed, including generated artifacts.
- Test, typechecking, and build commands with their results.
- Remaining work without starting it.

Do not fix review findings, modify files further, begin another slice, or create a commit. Stop and wait for the user's validation.
