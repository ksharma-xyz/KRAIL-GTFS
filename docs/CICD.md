# CI/CD Pipeline

## Overview

Automated workflow that generates GTFS data and deploys it to the KRAIL mobile app repository via Pull Requests.

---

## Workflow Architecture

```
┌─────────────────────────────────────────────────────┐
│ GitHub Actions Trigger                              │
│ - Schedule: Daily at 2 AM UTC                       │
│ - Manual: workflow_dispatch                         │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│ Step 1: Generate GTFS Data                          │
│ - Checkout KRAIL-GTFS repo                          │
│ - Run ./gradlew runKRAIL-GTFS                       │
│ - Produces: cache/*.pb files                        │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│ Step 2: Verify Generated Files                      │
│ - Check NSW_STOPS.pb exists                         │
│ - Check NSW_BUSES_ROUTES.pb exists                  │
│ - Validate file sizes                               │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│ Step 3: Create PR in KRAIL App Repo                 │
│ - Checkout ksharma-xyz/Krail                        │
│ - Copy NSW_STOPS.pb → io/gtfs/.../NSW_STOPS.pb     │
│ - Copy NSW_BUSES_ROUTES.pb → io/gtfs/...           │
│ - Bump NSW_STOPS_VERSION in SandookPreferences.kt   │
│ - Create PR with changes                            │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│ Step 4: Auto-Merge                                  │
│ - Enable auto-merge on PR                           │
│ - Merges when checks pass                           │
└─────────────────────────────────────────────────────┘
```

---

## Workflows

### 1. `update-krail-app.yml`

**Location:** `.github/workflows/update-krail-app.yml`

**Purpose:** Main workflow that orchestrates GTFS data generation and deployment

**Triggers:**
- `workflow_call` - Called by scheduled-tasks workflow
- `workflow_dispatch` - Manual trigger with inputs

**Key Steps:**

#### Download/Generate GTFS Data
```yaml
- name: Download GTFS artifacts (if available)
  uses: actions/download-artifact@v7
  continue-on-error: true
  with:
    name: generated-json-files
    path: cache

- name: Generate GTFS Data (fallback)
  if: steps.download-artifact.outcome != 'success'
  run: ./gradlew runKRAIL-GTFS
```

**Logic:**
- Tries to use cached artifacts from scheduled workflow
- Falls back to generating fresh data if artifacts unavailable/expired

#### Verify Files
```yaml
- name: Verify PB files exist
  run: |
    # Check NSW_STOPS.pb
    if [ ! -f "cache/NSW_STOPS.pb" ]; then
      echo "❌ ERROR: NSW_STOPS.pb not found!"
      exit 1
    fi
    
    # Check NSW_BUSES_ROUTES.pb
    if [ ! -f "cache/NSW_BUSES_ROUTES.pb" ]; then
      echo "❌ ERROR: NSW_BUSES_ROUTES.pb not found!"
      exit 1
    fi
```

**Validation:**
- Ensures both `.pb` files were generated successfully
- Fails workflow if files missing

#### Deploy to KRAIL App
```yaml
- name: Update KRAIL App
  uses: ./.github/actions/update-krail-app
  with:
    krail-repo: 'ksharma-xyz/Krail'
    stops-pb-file-path: cache/NSW_STOPS.pb
    routes-pb-file-path: cache/NSW_BUSES_ROUTES.pb
    run-number: ${{ github.run_number }}
```

---

### 2. `update-krail-app` Action

**Location:** `.github/actions/update-krail-app/action.yml`

**Purpose:** Reusable action that creates PR in KRAIL app repository

**Inputs:**
```yaml
stops-pb-file-path: 'cache/NSW_STOPS.pb'
routes-pb-file-path: 'cache/NSW_BUSES_ROUTES.pb'
target-stops-pb-path: 'io/gtfs/src/commonMain/composeResources/files/NSW_STOPS.pb'
target-routes-pb-path: 'io/gtfs/src/commonMain/composeResources/files/NSW_BUSES_ROUTES.pb'
preferences-file-path: 'sandook/src/commonMain/kotlin/xyz/ksharma/krail/sandook/SandookPreferences.kt'
```

**Steps:**

#### 1. Copy Protobuf Files
```bash
# Copy stops .pb file
cp cache/NSW_STOPS.pb krail-app/io/gtfs/.../NSW_STOPS.pb

# Copy routes .pb file
cp cache/NSW_BUSES_ROUTES.pb krail-app/io/gtfs/.../NSW_BUSES_ROUTES.pb
```

#### 2. Bump Version
```bash
# Extract current version
CURRENT_VERSION=$(grep -o 'const val NSW_STOPS_VERSION = [0-9]*L' SandookPreferences.kt | grep -o '[0-9]*')

# Increment
NEW_VERSION=$((CURRENT_VERSION + 1))

# Update file
sed -i "s/const val NSW_STOPS_VERSION = ${CURRENT_VERSION}L/const val NSW_STOPS_VERSION = ${NEW_VERSION}L/" SandookPreferences.kt
```

**Example:**
```kotlin
// Before
const val NSW_STOPS_VERSION = 42L

// After
const val NSW_STOPS_VERSION = 43L
```

#### 3. Check for Changes
```bash
git diff --quiet NSW_STOPS.pb NSW_BUSES_ROUTES.pb SandookPreferences.kt
if [ $? -eq 0 ]; then
  echo "No changes detected - skipping PR"
else
  echo "Changes detected - creating PR"
fi
```

**Logic:**
- Only creates PR if files actually changed
- Prevents empty PRs

#### 4. Create Pull Request
```bash
gh pr create \
  --title "Update NSW GTFS data (stops + routes)" \
  --body "Auto-generated update of NSW GTFS Protobuf files
  
**Changes:**
- Updated NSW_STOPS.pb with latest GTFS stops data
- Updated NSW_BUSES_ROUTES.pb with latest route-to-stops mappings
- Bumped NSW_STOPS_VERSION to **${NEW_VERSION}**

**What's included:**
- 🚏 Stops data: All NSW transport stops with coordinates
- 🚌 Routes data: Bus route numbers mapped to their stop sequences" \
  --label "auto-generated-gtfs" \
  --base main
```

**PR Example:**
```
Title: Update NSW GTFS data (stops + routes)

Body:
Auto-generated update from KRAIL-GTFS repository.

Changes:
- Updated NSW_STOPS.pb (37,738 stops)
- Updated NSW_BUSES_ROUTES.pb (4,702 routes)
- Bumped NSW_STOPS_VERSION to 43

Labels: auto-generated-gtfs
```

#### 5. Enable Auto-Merge
```bash
gh pr merge $PR_NUMBER --auto --squash
```

**Behavior:**
- PR merges automatically when all checks pass
- Uses squash merge to keep history clean

---

## Authentication

### GitHub App Token

**Why:** Standard GITHUB_TOKEN doesn't trigger workflows in target repo

**Solution:** Uses GitHub App for authentication
```yaml
- name: Generate GitHub App Token
  uses: actions/create-github-app-token@v2
  with:
    app-id: ${{ secrets.APP_ID }}
    private-key: ${{ secrets.APP_PRIVATE_KEY }}
    repositories: KRAIL-GTFS,Krail
```

**Permissions:**
- `contents: write` - Push to branches
- `pull-requests: write` - Create/manage PRs

---

## Secrets Required

| Secret | Purpose |
|--------|---------|
| `NSW_TRANSPORT_API_KEY` | Download GTFS data from NSW Transport |
| `APP_ID` | GitHub App ID for authentication |
| `APP_PRIVATE_KEY` | GitHub App private key |

---

## Error Handling

### Missing Files
```yaml
if [ ! -f "cache/NSW_STOPS.pb" ]; then
  echo "ERROR: NSW_STOPS.pb not found!"
  exit 1
fi
```
**Result:** Workflow fails, no PR created

### Existing PR
```bash
existing=$(gh pr list --label auto-generated-gtfs --state open)
if [[ -n "$existing" ]]; then
  echo "⚠️ Found existing PR - skipping"
  exit 0
fi
```
**Result:** Skips PR creation, workflow succeeds

### No Changes
```bash
if git diff --quiet; then
  echo "No changes detected - skipping PR"
fi
```
**Result:** No PR created, workflow succeeds

---

## Schedule

**Default:** Daily at 2 AM UTC

```yaml
on:
  schedule:
    - cron: '0 2 * * *'
```

**Why 2 AM?**
- Low traffic time
- NSW Transport data usually updates overnight
- Mobile app users unlikely to notice deployment

---

## Manual Trigger

```yaml
on:
  workflow_dispatch:
    inputs:
      krail-repo:
        description: 'KRAIL repository'
        default: 'ksharma-xyz/Krail'
```

**Usage:**
1. Go to Actions tab in GitHub
2. Select "Update KRAIL App"
3. Click "Run workflow"
4. (Optional) Change target repository

---

## Outputs

### Workflow Summary
```markdown
## KRAIL App Update Summary

✅ **PR Created Successfully**

- **PR URL:** https://github.com/ksharma-xyz/Krail/pull/123
- **PR Number:** #123
- **Version Bumped To:** 43

The PR has been configured for auto-merge and will be merged once checks pass.
```

### PR Content
- **Branch:** `update-gtfs-data-{run_number}`
- **Commits:** 1 squash commit
- **Files Changed:**
  - `io/gtfs/.../NSW_STOPS.pb`
  - `io/gtfs/.../NSW_BUSES_ROUTES.pb`
  - `sandook/.../SandookPreferences.kt`

---

## Monitoring

### Success Indicators
- ✅ Workflow completes successfully
- ✅ PR created in KRAIL repository
- ✅ Auto-merge enabled
- ✅ PR merges within minutes

### Failure Scenarios
- ❌ GTFS download fails → Retry with `refresh = true`
- ❌ Files not generated → Check Main.kt logs
- ❌ PR creation fails → Check GitHub App permissions
- ❌ No changes detected → Normal, no action needed

