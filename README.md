# My Blog

A markdown-first blog powered by Jekyll and GitHub Pages.

## 🚀 Quick Start

### 1. Create a New Post

Create a file in `_posts/` with the format: `YYYY-MM-DD-title.md`

```markdown
---
layout: post
title: "Your Post Title"
date: 2026-01-08
categories: [category1, category2]
tags: [tag1, tag2]
excerpt: "Brief description for previews"
---

Your content here in Markdown...
```

### 2. Push to GitHub

```bash
git add .
git commit -m "New post: Your title"
git push origin main
```

### 3. Automatic Deployment

GitHub Actions will automatically build and deploy to GitHub Pages.

Your blog will be live at: `https://<username>.github.io/<repo-name>/`

## 📁 Structure

```
├── _posts/              # Your blog posts (YYYY-MM-DD-title.md)
├── _config.yml          # Jekyll configuration
├── assets/images/       # Images for posts
├── index.md             # Home page
└── .github/workflows/   # Auto-deploy workflow
```

## 🔧 Setup (First Time)

1. Create a GitHub repository
2. Push this code
3. Go to **Settings → Pages → Source → GitHub Actions**
4. Wait for the first deployment

## ✍️ Cross-Posting to Medium

Since Medium no longer provides API tokens:

1. Write your post in `_posts/`
2. Push to GitHub → auto-deploys to your site
3. On Medium: **Import a story** → paste your GitHub Pages URL
   - Or manually copy the markdown content
4. Set the **canonical URL** in Medium to your GitHub Pages post URL

> **Tip**: Setting the canonical URL tells search engines your site is the original source.

## 🎨 Customization

Edit `_config.yml` to change:
- Site title & description
- Author name
- Theme settings
- Permalink structure

## 📝 Local Development

```bash
# Install dependencies
bundle install

# Run local server
bundle exec jekyll serve

# View at http://localhost:4000
```
# a-dev-writes
