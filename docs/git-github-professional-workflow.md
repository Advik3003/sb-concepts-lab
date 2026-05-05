# Git and GitHub Professional Workflow Tutorial

This tutorial explains how to use Git and GitHub in a professional software engineering workflow. The focus is clean history, safe collaboration, reviewable pull requests, and fewer production mistakes.

## Core Concepts

Git is the local version control system. GitHub is the remote collaboration platform where teams host repositories, review pull requests, run CI checks, track issues, and protect important branches.

Use Git for:

- Tracking code changes locally.
- Creating branches for isolated work.
- Comparing, staging, committing, reverting, and merging changes.
- Recovering previous versions of files.

Use GitHub for:

- Remote repository hosting.
- Pull requests and code review.
- Branch protection rules.
- CI/CD checks.
- Issues, releases, tags, and team collaboration.

## Daily Professional Workflow

Start every task from a clean, updated branch.

```powershell
git status
git switch main
git pull --ff-only
git switch -c feature/add-profile-docs
```

Use branch names that describe the work:

```text
feature/add-swagger-docs
fix/localstack-profile-port
docs/update-logging-guide
refactor/cloud-client-interface
```

Avoid vague names:

```text
changes
final
test
my-work
```

## Check Status Often

Use `git status` before and after meaningful work.

```powershell
git status
```

This tells you:

- Which files changed.
- Which files are staged.
- Which files are untracked.
- Whether your branch is ahead, behind, or diverged.

Use `git diff` to review unstaged changes:

```powershell
git diff
```

Use `git diff --staged` to review what will be committed:

```powershell
git diff --staged
```

## Stage Changes Carefully

Stage only files that belong to the current task.

```powershell
git add docs/git-github-professional-workflow.md
git status
```

Avoid blindly staging everything unless you have reviewed every changed file:

```powershell
git add .
```

`git add .` is acceptable only when the working tree is understood and all changes belong together.

## Write Good Commits

A professional commit should be focused, understandable, and reversible.

Good commit messages:

```text
Add Git and GitHub workflow guide
Fix LocalStack profile port documentation
Refactor cloud environment client selection
```

Weak commit messages:

```text
update
changes
fix
final
```

Commit after reviewing the staged diff:

```powershell
git diff --staged
git commit -m "Add Git and GitHub workflow guide"
```

## Commit Size

Keep commits small enough to review.

Good commit boundaries:

- One bug fix.
- One documentation page.
- One focused refactor.
- One feature slice with tests.

Avoid mixing:

- Feature logic and unrelated formatting.
- Refactor and behavior change unless the refactor is required.
- Multiple unrelated bug fixes.
- Generated files and hand-written changes without explanation.

## Keep Secrets Out Of Git

Never commit:

- `.env` files with real values.
- AWS access keys or secret keys.
- GitHub tokens.
- Database passwords.
- Private certificates or SSH keys.
- Production credentials.

Before committing, check:

```powershell
git diff --staged
```

If a secret was committed, do not only delete it in a later commit. Treat it as exposed, rotate the secret, and remove it from history using an approved team process.

## Work With Remote Branches

Push your branch to GitHub:

```powershell
git push -u origin feature/add-profile-docs
```

After the first push, future pushes can use:

```powershell
git push
```

Fetch remote updates without changing your working tree:

```powershell
git fetch origin
```

Update your branch from `main` when needed:

```powershell
git switch main
git pull --ff-only
git switch feature/add-profile-docs
git merge main
```

Some teams prefer rebase for a linear history:

```powershell
git fetch origin
git rebase origin/main
```

Use the strategy your team standardizes on. Do not rebase shared branches unless your team explicitly allows it.

## Pull Request Workflow

A pull request should explain why the change exists and how it was verified.

Good PR title:

```text
Add professional Git and Swagger documentation
```

Good PR description:

```markdown
## Summary
- Adds Git and GitHub workflow guidance for daily development.
- Adds Swagger UI documentation for same-origin and cross-origin setups.

## Test Plan
- Documentation-only change.
- Checked Markdown diagnostics in edited files.
```

Before opening a PR:

- Run relevant tests.
- Review the full diff.
- Remove temporary debug code.
- Confirm no secrets are present.
- Keep the PR focused.
- Update docs when behavior changes.

## Code Review Etiquette

As an author:

- Keep the PR small enough to review.
- Explain trade-offs and known limitations.
- Respond to comments with context.
- Push follow-up commits instead of hiding changes.
- Do not mark conversations resolved until the concern is actually addressed.

As a reviewer:

- Prioritize correctness, security, maintainability, and missing tests.
- Distinguish required changes from optional suggestions.
- Be specific and cite the relevant code.
- Avoid style-only comments if the project has automated formatting.

## Branch Protection

Professional repositories usually protect `main` or `master`.

Recommended GitHub rules:

- Require pull request review before merge.
- Require CI checks to pass.
- Require branch to be up to date before merge when needed.
- Restrict force pushes.
- Restrict direct pushes to protected branches.
- Require signed commits only if the team is prepared to support them.

The goal is not bureaucracy. The goal is to prevent accidental production changes.

## Merge Strategies

Common GitHub merge options:

| Strategy | Best For | Notes |
| --- | --- | --- |
| Merge commit | Preserving branch history | Clear PR boundary, more merge commits. |
| Squash merge | Small PRs with many work-in-progress commits | Clean main history, loses individual branch commits. |
| Rebase merge | Linear history | Requires discipline around conflict resolution. |

For many teams, squash merge is a good default for feature branches because the final `main` history stays readable.

## Tags And Releases

Use tags for release points:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Use annotated tags when release metadata matters:

```powershell
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

Professional release notes should include:

- User-facing changes.
- Bug fixes.
- Migration steps.
- Known issues.
- Rollback notes when needed.

## Undoing Changes Safely

Discard an unstaged change only when you are sure it is not needed:

```powershell
git restore path/to/file
```

Unstage a file without deleting the change:

```powershell
git restore --staged path/to/file
```

Create a new commit that reverses an earlier commit:

```powershell
git revert <commit-sha>
```

Prefer `git revert` for shared history. Avoid destructive commands like hard reset on shared branches unless your team explicitly approves the operation.

## Handling Conflicts

Conflicts happen when two changes touch the same lines or nearby structure.

Professional conflict process:

1. Understand both sides of the change.
2. Keep the intended behavior from each branch.
3. Run tests after resolving.
4. Review the resolved diff carefully.
5. Ask the original author when business logic is unclear.

Do not resolve conflicts by blindly choosing "ours" or "theirs" unless the situation is fully understood.

## Useful Commands

```powershell
git status
git diff
git diff --staged
git log --oneline --decorate --graph --all
git branch
git switch <branch-name>
git switch -c <new-branch-name>
git fetch origin
git pull --ff-only
git push
git restore <file>
git restore --staged <file>
```

## Professional Checklist

Before pushing:

- `git status` is understood.
- The diff contains only intended changes.
- Tests or relevant checks passed.
- No secrets are included.
- Commit message explains the purpose.

Before merging:

- PR is reviewed.
- CI is green.
- Conflicts are resolved correctly.
- Documentation is updated when needed.
- The change can be rolled back if required.

Git and GitHub are not only tools for saving code. Used professionally, they are a safety system for collaboration, review, release management, and production reliability.

## Hinglish Summary

Git local version control tool hai, aur GitHub remote collaboration platform hai. Professional workflow ka goal hai clean history, safe collaboration, reviewable pull requests, aur production mistakes ko reduce karna.

Daily workflow:

```powershell
git status
git switch main
git pull --ff-only
git switch -c feature/add-new-docs
```

Kaam karte waqt frequently check karo:

```powershell
git status
git diff
git diff --staged
```

Commit karne se pehle staged diff review karna important hai. Sirf wahi files stage karo jo current task ka part hain.

Good commit message:

```text
Add Swagger UI CORS guide
Fix LocalStack profile documentation
Update secure properties tutorial
```

Weak commit message:

```text
update
fix
changes
final
```

Professional GitHub PR me ye information honi chahiye:

- Change ka short summary.
- Test plan ya verification steps.
- Known limitations agar koi hain.
- Reviewer ke liye context.

Secrets kabhi commit mat karo:

- `.env` real values ke saath
- AWS keys
- GitHub tokens
- Database passwords
- Private certificates

Shared branch history me destructive commands carefully use karo. Team branch par `git revert` safer hota hai because woh old commit ko undo karne ke liye new commit create karta hai.

Short recommendation: har task ke liye separate branch banao, diff review karo, focused commit rakho, PR open karo, CI green rakho, aur merge se pehle review complete karo.
