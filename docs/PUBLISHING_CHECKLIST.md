# Publishing Checklist

> Documentation axis: checks applied when a Mystro commit is published to the remote repository.

1. Confirm that the intended version is set in `pom.xml`.
2. Run `mvn test` and `mvn package -DskipTests`.
3. Apply the privacy rules in [TEST_DATA_POLICY.md](TEST_DATA_POLICY.md).
4. Inspect the complete staging area with:

   ```bash
   git status --short
   git diff --cached --stat
   git diff --cached --check
   ```

5. Inspect tracked runtime paths; the command should return an empty result:

   ```bash
   git ls-files ephe dll libs output target native-list.json
   ```

6. Confirm that the staging area contains only the intended source, tests, and documentation.
7. Commit with a message that describes the calculation or contract change.
8. Push the selected branch.
9. After pushing, verify that the local branch and its upstream resolve to the same commit.
