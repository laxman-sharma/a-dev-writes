# Medium Blog Publishing

Automatically publish your Markdown articles to Medium using GitHub Actions.

## 🚀 Quick Start

1. **Add your Medium Integration Token**
   - Go to [Medium Settings → Security and apps](https://medium.com/me/settings/security)
   - Generate an **Integration Token**
   - Add it to your GitHub repo: **Settings → Secrets → Actions** → `MEDIUM_INTEGRATION_TOKEN`

2. **Write your article**
   ```bash
   cp posts/_template.md posts/my-first-article.md
   # Edit your article
   ```

3. **Publish**
   - **Auto-publish**: Push/merge to `main` branch → publishes as draft
   - **Manual**: Go to **Actions** → **Publish to Medium** → **Run workflow**

## 📁 Structure

```
├── posts/
│   ├── _template.md      # Copy this for new articles
│   └── my-article.md     # Your articles go here
├── .github/
│   └── workflows/
│       └── publish-to-medium.yml
└── README.md
```

## ✍️ Article Format

```markdown
---
title: "Your Article Title"
tags: ["programming", "tutorial"]
canonicalUrl: "https://yourblog.com/original-post"  # Optional
---

# Your Article Title

Your content here...
```

## ⚙️ Configuration

### Publish to a Publication

1. Get your **Publication ID** from Medium
2. Add secret: `MEDIUM_PUBLICATION_ID`
3. Uncomment `publication_id` in `.github/workflows/publish-to-medium.yml`

### Secrets Required

| Secret | Required | Description |
|--------|----------|-------------|
| `MEDIUM_INTEGRATION_TOKEN` | ✅ Yes | Your Medium integration token |
| `MEDIUM_PUBLICATION_ID` | ❌ Optional | For publishing to a publication |

## 📝 Publishing Options

| Trigger | Status | Description |
|---------|--------|-------------|
| Push to `main` | Draft | Safe - review on Medium before publishing |
| Manual workflow | Selectable | Choose: draft, public, or unlisted |

## 🔗 Useful Links

- [Medium API Documentation](https://github.com/Medium/medium-api-docs)
- [Generate Integration Token](https://medium.com/me/settings/security)
