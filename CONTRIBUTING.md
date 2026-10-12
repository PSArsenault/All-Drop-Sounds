# Contributing

Pull requests follow the same model as RuneLite, the Plugin Hub, and plugins such as Quest Helper: one long-lived branch, and a short-lived branch for each change.

## Branches

`master` is the only long-lived branch. Open every pull request against `master`. A Plugin Hub release is a commit on `master`, not a separate release branch.

`main` stays aligned with `master` so existing clones keep working. Do not open pull requests against `main`.

Start each change from the latest `master`:

- `feat/short-description` for a new behavior or setting
- `fix/short-description` for a bug
- `docs/short-description` for documentation only

Keep one change on a branch. Delete the branch after the pull request merges.

## Pull requests

Describe what changed and why. List how you tested it. Run `gradle test`. When the change is audible or visible in game, say what you clicked and what you heard.

Do not commit `.gradle/` or `build/`.

When a change affects behavior, settings, sound files, or how to build and run, update `README.md` in the same pull request. Save `README.md` as UTF-8 without a byte-order mark.
