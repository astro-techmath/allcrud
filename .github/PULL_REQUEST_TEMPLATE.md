## Summary

@coderabbitai summary

## Type of change

- [ ] `feat` — new feature
- [ ] `fix` — bug fix
- [ ] `docs` — documentation only
- [ ] `refactor` — no functional change
- [ ] `test` — test-only change
- [ ] `chore` — tooling/build/dependency change

## Testing

<!-- How was this verified? For any change with runtime implications, "it compiles" is not sufficient - see AGENTS.md. -->

- [ ] Real compilation and/or real Spring context validation done for any
  runtime-affecting change (not reflection/mocks alone)
- [ ] Existing tests pass
- [ ] New tests added where applicable

## Checklist

- [ ] Comments are in English
- [ ] No dead code left behind from a superseded decision
- [ ] Public API changes (AbstractEntity, AbstractEntityVO, CrudController,
  CrudService, etc.) are backward compatible, or the break is
  explicitly called out above