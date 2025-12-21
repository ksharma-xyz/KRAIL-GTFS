# Update KRAIL App Repository Action

This composite action automates the process of updating the KRAIL app repository with the latest NSW GTFS stops data.

## What it does

1. ✅ Verifies the generated `.pb` file exists
2. ✅ Checks out the KRAIL repository
3. ✅ Copies the `NSW_STOPS.pb` file to the correct location
4. ✅ Automatically increments the `NSW_STOPS_VERSION` constant
5. ✅ Creates a pull request with both changes
6. ✅ Enables auto-merge on the PR

## Usage

```yaml
- name: Update KRAIL App
  uses: ./.github/actions/update-krail-app
  with:
    krail-repo: 'ksharma-xyz/Krail'
    github-token: ${{ secrets.GITHUB_TOKEN }}
    pb-file-path: 'cache/NSW_STOPS.pb'
    run-number: ${{ github.run_number }}
```

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `krail-repo` | KRAIL repository in owner/repo format | No | `ksharma-xyz/Krail` |
| `github-token` | GitHub token with permissions to create PRs | Yes | - |
| `pb-file-path` | Path to the NSW_STOPS.pb file to copy | No | `cache/NSW_STOPS.pb` |
| `target-pb-path` | Target path in KRAIL repo for the .pb file | No | `io/gtfs/src/commonMain/composeResources/files/NSW_STOPS.pb` |
| `preferences-file-path` | Path to SandookPreferences.kt in KRAIL repo | No | `sandook/src/commonMain/kotlin/xyz/ksharma/krail/sandook/SandookPreferences.kt` |
| `run-number` | GitHub run number for branch naming | Yes | - |

## Outputs

| Output | Description |
|--------|-------------|
| `pr-created` | Whether a PR was created (true/false) |
| `pr-url` | URL of the created PR |
| `pr-number` | Number of the created PR |
| `version-bumped` | New version number after bump |

## Example with outputs

```yaml
- name: Update KRAIL App
  id: update-krail
  uses: ./.github/actions/update-krail-app
  with:
    github-token: ${{ secrets.GITHUB_TOKEN }}
    run-number: ${{ github.run_number }}

- name: Show results
  run: |
    echo "PR Created: ${{ steps.update-krail.outputs.pr-created }}"
    echo "PR URL: ${{ steps.update-krail.outputs.pr-url }}"
    echo "New Version: ${{ steps.update-krail.outputs.version-bumped }}"
```

## Requirements

- The `NSW_STOPS.pb` file must exist at the specified path
- The GitHub token must have write permissions to the KRAIL repository
- The KRAIL repository must have the expected file structure

## Version Bumping

The action automatically finds and increments the `NSW_STOPS_VERSION` constant:

**Before:**
```kotlin
const val NSW_STOPS_VERSION = 14L
```

**After:**
```kotlin
const val NSW_STOPS_VERSION = 15L
```

## PR Creation

The action creates a PR with:
- Label: `auto-generated-gtfs`
- Title: "Update NSW GTFS stops data"
- Auto-merge enabled (squash merge)
- Detailed description with links back to the source workflow

If an existing PR with the `auto-generated-gtfs` label is already open, no new PR is created.
