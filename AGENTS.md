# General agent workflow and efficiency

These rules apply to work in this repository regardless of the project being developed.

## Work selection
- Maintain one clear, prioritised work queue. Select the highest-priority task whose dependencies are satisfied.
- Complete one coherent vertical slice at a time: implementation, focused regression tests, and required documentation.
- Before starting, identify the owning files, public interfaces, callers, dependencies, and existing tests.
- Avoid speculative refactors, duplicate abstractions, unnecessary frameworks, and unrelated changes.

## Efficient investigation
- Read only the documentation, code, tests, and data formats needed for the current task.
- Do not repeatedly inspect unchanged files or unrelated modules.
- Parallelise independent read-only investigations and test analysis.
- Ask delegated agents for concise findings with exact file paths, symbols, evidence, risks, and a recommended next action.
- Treat agent summaries as leads, not proof; verify important claims against source and test results.

## Agent coordination
- Give each concurrent implementation effort exclusive ownership of its files.
- Serialize overlapping edits, integration, and commits.
- Define each delegated task with a bounded scope, expected output, and clear completion criteria.
- Share discoveries promptly so agents do not repeat the same investigation.
- Integrate completed work in dependency order and resolve conflicts centrally.

## Implementation and validation
- Prefer extending existing abstractions over introducing parallel implementations.
- Run the narrowest relevant test after each logical change.
- Run broader validation once the complete change is assembled.
- Do not rebuild unchanged modules or rerun successful checks without a code or configuration reason.
- Never claim a build, test, deployment, or other operation succeeded unless its actual result was checked.

## Review and delivery
- Review one final diff against the starting commit.
- Remove temporary files, duplicate checks, dead code, and stale work-queue entries.
- Batch related implementation, tests, and documentation into a complete, reviewable commit.
- Avoid intermediate commits that leave the repository inconsistent.
- After pushing, verify the pushed commit and any relevant CI result.
- End each handoff with the completed work, commit, checks performed, remaining blockers, and next actionable task.
