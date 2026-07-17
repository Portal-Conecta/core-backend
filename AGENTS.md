# Agent Instructions

Act as a senior software engineer in this repository. First inspect the relevant code, tests, and contribution rules. Prefer the smallest correct solution that fits the existing architecture and conventions. Reuse local utilities and patterns, avoid unnecessary dependencies, and do not change unrelated code.

## Default Issue Workflow

When the user asks you to work on an issue, treat it as a request to complete the whole local implementation workflow unless they explicitly ask for something narrower.

1. Read the issue and identify the goal, acceptance criteria, affected behavior, likely files, risks, and out-of-scope items.
2. Read `README.md`, `CONTRIBUTING.md`, `.github/pull_request_template.md`, and the code/tests related to the affected domain.
3. Check the current git status before editing. Preserve unrelated user changes.
4. Create a local branch from the repository's base branch, using the issue number and the repository's observed branch style:
   - `feature/#123-short-description` for new functionality;
   - `fix/#123-short-description` for bug fixes;
   - `refactor/#123-short-description` for refactors;
   - `docs/#123-short-description` for documentation-only work.
5. Use `develop` as the base branch when the user or issue does not specify another base.
6. Implement the required changes completely, including production code, tests, and documentation when applicable.
7. Validate the change with focused tests first, then run the project-level test command when the change affects backend behavior.
8. Review the diff before committing. Remove temporary files, generated build output, secrets, debug code, and unrelated edits.
9. Create local commits only. Keep commits small, semantic, atomic, and easy to review.
10. Create a local, untracked pull request description text file based on `.github/pull_request_template.md`.
11. Stop before push. Do not push, open a pull request, or change remote state unless the user explicitly asks.

## Commit Standard

Use Conventional Commits and match the repository history. When an issue number exists, prefix every commit with it:

```text
#123 feat: adiciona endpoint de exemplo
#123 fix: corrige validacao de exemplo
#123 test: cobre regra de exemplo
#123 docs: documenta comportamento de exemplo
```

Rules:

- Use the issue number at the beginning of each commit message when working from an issue.
- Keep commit descriptions short, lowercase, and in Portuguese, matching the existing history.
- Split commits by intent. Production code, tests, documentation, and independent refactors should be separate commits when that makes the review clearer.
- Do not use vague messages such as `wip`, `ajustes`, `corrige`, `final`, or `commit final`.
- Do not create one large catch-all commit when the work can be cleanly split.

## Validation Standard

- For backend changes, run at least `.\mvnw.cmd test` on Windows or `./mvnw test` on Unix, unless there is a technical blocker or the user explicitly asks not to.
- Add or update tests for business rules, bug fixes, authorization changes, persistence behavior, API contracts, and other user-visible behavior.
- If a full test run is too expensive or blocked, run the most relevant narrower tests and clearly report what was and was not validated.
- Do not mark the task complete while known failing tests remain unexplained.

## Git Safety

- Never commit directly to `main`, `dev`, or `develop`.
- Never push, open pull requests, force-push, rebase shared branches, or alter remote state without explicit user approval.
- Never revert or overwrite existing user changes unless the user explicitly asks.
- If the working tree is dirty, separate your own changes from pre-existing changes and mention anything relevant in the final summary.
- If you cannot read the issue through available tools, ask the user for the issue link or content before implementing.

## Final Response

When finished, report:

- branch name;
- local commits created;
- tests or validation commands executed;
- local untracked pull request description file created;
- any files intentionally left uncommitted;
- blockers, skipped validation, or follow-up work, if any.

Always leave the pull request description ready as a local text file, but do not stage or commit it. Use the repository template as the base and fill it with the actual objective, main changes, review notes, validation steps, risks, and related issue.
