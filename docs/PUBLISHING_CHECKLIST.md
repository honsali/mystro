# Publishing checklist

Run this checklist after `git init` and before the first push.

1. Stage normally with `git add .`; never use `git add -f` for ignored files.
2. Inspect every staged path with `git status --short` and `git diff --cached --stat`.
3. Confirm that `git ls-files` contains none of the following:
   - `native-list.json`;
   - `output/` or `target/`;
   - `SESSION_MEMORY.md` or another session transcript;
   - `.settings/`, `.vscode/`, `.agents/`, or `.codex/`.
4. Search `README.md`, `docs/`, and `src/` separately for every private alias, birth-date
   representation, birth time, latitude, and longitude. Do not rely on one combined search.
5. Review natal snapshots as sensitive data too: changing only a subject name is not enough.
6. Run `mvn test` and `mvn package -DskipTests`.
7. Inspect the final commit from a fresh clone before making the new repository public.

Keep the private search markers outside this repository; committing them in a checking script would
reintroduce the data the check is meant to protect.
