---
layout: dsa_post
course: opentelemetry
title: "Module 2: Implementation"
category: "Module 2: Implementation"
permalink: /opentelemetry/module-2-implementation/
---
# Module 2: Implementation

Get your hands dirty. This module covers the core API calls you'll use daily to add context and visibility to your code.

{% assign modules = site.documents | where: "collection", "opentelemetry" | where: "category", "Module 2: Implementation" | sort: "order" %}
<div class="dsa-module-list">
    {% for item in modules %}
    <a href="{{ item.url | relative_url }}" class="dsa-module-item">
        <span class="dsa-item-order">{{ item.order }}</span>
        <div class="dsa-item-content">
            <h3>{{ item.title }}</h3>
            <p>{{ item.excerpt }}</p>
        </div>
        <span class="dsa-item-arrow">→</span>
    </a>
    {% endfor %}
</div>
