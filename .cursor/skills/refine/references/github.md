# GitHub CLI recipes (refine)

Use **`gh`**. Do not depend on GitHub MCP or GitLens issue tools (they may be unauthenticated).

Default repo: `adam-k-ali/DWM`.
Project: [users/adam-k-ali/projects/7](https://github.com/users/adam-k-ali/projects/7) (`DWM`).

IDs (user project 7 — re-check with `gh project view` / `field-list` if commands fail):

| Thing | Value |
|-------|--------|
| Project number | `7` |
| Project node id | `PVT_kwHOAjGLjc4BgoYO` |
| Status field | `PVTSSF_lAHOAjGLjc4BgoYOzhfnM0Q` |
| Status Ready | `61e4505c` |
| Status Backlog | `f75ad846` |
| Status In progress | `47fc9ee4` |
| Status Done | `98236657` |
| Status Cancelled | `754d467e` |
| Priority field | `PVTSSF_lAHOAjGLjc4BgoYOzhfnNFQ` |
| Size field | `PVTSSF_lAHOAjGLjc4BgoYOzhfnNFU` |

Do not change Priority or Size during refine unless the user asks.

---

## Resolve identifier → issue number

```bash
# URL or #N — use N directly
gh issue view 202 --repo adam-k-ali/DWM --json number,title,url

# Stable ID in title
gh issue list --repo adam-k-ali/DWM --search 'in:title DWM-061' --limit 5 --json number,title,state,url
gh issue list --repo adam-k-ali/DWM --search 'in:title E-006' --limit 5 --json number,title,state,url
```

If multiple matches, pick the open issue whose title starts with that ID (`DWM-061 — …`). Stop if none.

---

## Fetch issue (Phase 1)

```bash
gh issue view 202 --repo adam-k-ali/DWM \
  --json number,title,body,comments,labels,parent,projectItems,url,state,closedAt
```

Comments are in `.comments`. Parent epic is `.parent` (number, title, url).

Fetch each linked `#N` / `DWM-` sibling the same way. Do not invent bodies for issues `gh` cannot load.

---

## Project membership

`projectItems` on `issue view` includes Status name. If empty, the issue is not on project 7.

Add **only if the user asks**:

```bash
gh project item-add 7 --owner adam-k-ali --url https://github.com/adam-k-ali/DWM/issues/202
```

---

## Update description (Phase 4)

Write markdown to a temp file (no secrets). Then:

```bash
gh issue edit 202 --repo adam-k-ali/DWM --body-file /tmp/dwm-refine-202.md
```

Do not pass `--title` unless the user asked to rename.

Verify:

```bash
gh issue view 202 --repo adam-k-ali/DWM --json title,body,url,projectItems
```

---

## Set Status → Ready (only if confirmed this run)

Need the **project item id** (`PVTI_…`), not the issue number:

```bash
gh project item-list 7 --owner adam-k-ali --format json --limit 100 \
  --jq '.items[] | select(.content.number==202) | {id, status, title}'
```

If not found, raise `--limit` (board has dozens of items) or add `--query 'DWM-061'`.

Then:

```bash
gh project item-edit \
  --id PVTI_… \
  --project-id PVT_kwHOAjGLjc4BgoYO \
  --field-id PVTSSF_lAHOAjGLjc4BgoYOzhfnM0Q \
  --single-select-option-id 61e4505c
```

`--single-select-option-id` is the Ready option id (`61e4505c`). Re-`issue view` and confirm `projectItems[].status.name` is `Ready`.

---

## Token scopes

Issue view/edit needs the usual `gh` GitHub auth. Project field edits need the `project` scope:

```bash
gh auth status
# If project calls 403: gh auth refresh -s project
```

Do not run `gh auth refresh` unless the user can complete the browser/device flow.

---

## Screenplay-only issues

If the issue lives in another repo under the same owner, pass that `--repo`. Still use project 7 only when the issue is (or should be) on the DWM board.
