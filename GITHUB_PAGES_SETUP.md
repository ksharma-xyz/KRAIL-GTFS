# GitHub Pages Setup Instructions

## Prerequisites

MkDocs with Material theme has been configured for this repository. Follow these steps to enable GitHub Pages.

---

## GitHub Repository Settings

### 1. Enable GitHub Pages

1. Go to your repository: **https://github.com/ksharma-xyz/KRAIL-GTFS**
2. Click **Settings** tab
3. Scroll down to **Pages** section (left sidebar)
4. Under **Source**, select:
   - **Source:** Deploy from a branch
   - **Branch:** `gh-pages`
   - **Folder:** `/ (root)`
5. Click **Save**

### 2. Verify Deployment

After pushing documentation changes:

1. Go to **Actions** tab
2. Look for "Deploy Documentation" workflow
3. Wait for it to complete (green checkmark)
4. Visit: **https://ksharma-xyz.github.io/KRAIL-GTFS/**

---

## Local Development

### Install MkDocs

If `pip` is available on your system you can use the simple command:

```bash
# Install MkDocs with Material theme
pip install mkdocs-material pymdown-extensions

# Verify installation
mkdocs --version
```

If you see `zsh: command not found: pip`, try one of these alternatives (recommended):

1) Use python's -m pip (works even if `pip`/`pip3` binaries aren't on PATH):

```bash
python3 --version
python3 -m pip install --upgrade pip setuptools wheel
python3 -m pip install --user mkdocs-material pymdown-extensions
# If `mkdocs` binary not found, add user base bin to PATH (see next step)
python3 -m site --user-base
# Example: add to shell rc (zsh):
# export PATH="$HOME/Library/Python/3.10/bin:$PATH"
```

2) Use a virtual environment (isolated, recommended for projects):

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install mkdocs-material pymdown-extensions
mkdocs --version
```

3) If Python isn't installed (or you prefer Homebrew):

```bash
brew install python
pip3 install mkdocs-material pymdown-extensions
mkdocs --version
```

> Note: On macOS the user-local pip installs place binaries in `~/Library/Python/<version>/bin`. If `mkdocs` is not found after installing with `--user`, add that path to your shell's PATH.


### Preview Locally

```bash
# Serve documentation locally
mkdocs serve

# Opens at: http://127.0.0.1:8000
```

**Hot reload:** Changes to `docs/` automatically refresh the browser.

### Build Locally

```bash
# Build static site
mkdocs build

# Output: site/ directory
```

---

## Workflow Triggers

Documentation deploys automatically when:

- **Push to main** with changes in:
  - `docs/**` (any markdown file)
  - `mkdocs.yml` (configuration)
  - `.github/workflows/deploy-docs.yml` (workflow itself)

- **Manual trigger**:
  1. Go to Actions → Deploy Documentation
  2. Click "Run workflow"

---

## Configuration

### Theme Settings

**File:** `mkdocs.yml`

**Default theme:** Dark mode (slate)

**Toggle:** Users can switch between dark/light mode using the theme toggle button.

**Colors:**
- Primary: Indigo
- Accent: Indigo

### Navigation

Pages appear in this order:
1. Home (index.md)
2. Architecture
3. GTFS Processing
4. Data Formats
5. CI/CD Pipeline

### Features Enabled

- ✅ Navigation tabs
- ✅ Section navigation
- ✅ Back to top button
- ✅ Search with suggestions
- ✅ Code copy button
- ✅ Mermaid diagrams
- ✅ Admonitions (info/warning boxes)

---

## Customization

### Change Theme Colors

Edit `mkdocs.yml`:

```yaml
theme:
  palette:
    - scheme: slate
      primary: blue  # Change this
      accent: cyan   # And this
```

**Available colors:**
red, pink, purple, deep purple, indigo, blue, light blue, cyan, teal, green, light green, lime, yellow, amber, orange, deep orange

### Add New Pages

1. Create markdown file in `docs/`:
   ```bash
   touch docs/NewPage.md
   ```

2. Add to `mkdocs.yml` navigation:
   ```yaml
   nav:
     - Home: index.md
     - New Page: NewPage.md  # Add here
     - Architecture: Architecture.md
   ```

### Enable/Disable Features

Edit `mkdocs.yml`:

```yaml
theme:
  features:
    - navigation.tabs       # Top-level tabs
    - navigation.sections   # Expandable sections
    - navigation.top        # Back to top button
    - search.suggest        # Search suggestions
    - content.code.copy     # Copy code button
```

---

## Troubleshooting

### Documentation not updating

1. Check Actions tab for errors
2. Verify `gh-pages` branch exists
3. Clear browser cache
4. Wait 1-2 minutes for GitHub CDN

### Build fails locally

```bash
# Reinstall dependencies
python3 -m pip uninstall mkdocs-material -y
python3 -m pip install mkdocs-material pymdown-extensions
```

### 404 Error on GitHub Pages

1. Check Settings → Pages → Source is `gh-pages` branch
2. Verify workflow ran successfully
3. Check repository is public (or Pages enabled for private)

---

## GitHub Pages URL

Once configured, documentation will be available at:

**https://ksharma-xyz.github.io/KRAIL-GTFS/**

---

## Additional Resources

- [MkDocs Material Documentation](https://squidfunk.github.io/mkdocs-material/)
- [MkDocs Documentation](https://www.mkdocs.org/)
- [GitHub Pages Guide](https://docs.github.com/en/pages)

