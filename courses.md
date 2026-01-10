---
layout: page
title: Courses
permalink: /courses/
---

Welcome to our curated learning tracks. These series are designed to take you deep into specific technical topics with structured modules and hands-on examples.

<div class="courses-grid">
    <!-- DSA Course Card -->
    <a href="{{ '/dsa/' | relative_url }}" class="course-card dsa-card">
        <div class="course-icon">🧩</div>
        <div class="course-content">
            <h3>Data Structures & Algorithms</h3>
            <p>Master the fundamental building blocks of software. From Arrays to Graphs, geared for interview prep and deep understanding.</p>
            <span class="course-link">Start Learning &rarr;</span>
        </div>
    </a>

    <!-- OpenTelemetry Card -->
    <a href="{{ '/opentelemetry/' | relative_url }}" class="course-card otel-card">
        <div class="course-icon">🔭</div>
        <div class="course-content">
            <h3>OpenTelemetry with Java</h3>
            <p>Production-grade observability. Learn how to implement Tracing, Metrics, and Logs in distributed Java systems.</p>
            <span class="course-link">Explore Series &rarr;</span>
        </div>
    </a>
</div>

<style>
    .courses-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 2rem;
        margin-top: 2rem;
    }

    .course-card {
        background: var(--bg-card);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-lg);
        padding: 2rem;
        text-decoration: none;
        transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        height: 100%;
    }

    .course-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-md);
        border-color: var(--accent-primary);
    }

    .course-icon {
        font-size: 3rem;
        background: var(--bg-elevated);
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 0.5rem;
    }

    .dsa-card:hover .course-icon {
        background: rgba(59, 130, 246, 0.1); /* Blue tint */
    }

    .otel-card:hover .course-icon {
        background: rgba(168, 85, 247, 0.1); /* Purple tint */
    }

    .course-content h3 {
        margin: 0 0 0.5rem;
        font-size: 1.5rem;
        color: var(--text-primary);
    }

    .course-content p {
        color: var(--text-secondary);
        margin: 0;
        line-height: 1.6;
        flex-grow: 1;
    }

    .course-link {
        color: var(--accent-primary);
        font-weight: 600;
        margin-top: 1rem;
        display: inline-block;
    }
</style>
