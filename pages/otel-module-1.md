---
layout: dsa_post
title: "Module 1: Foundations"
permalink: /opentelemetry/module-1-foundations/
course: opentelemetry
category: "Module 1: Foundations"
---
# Module 1: Foundations

Lay the groundwork for Observability. We start with the "Why" and "What" before diving into the "How".

{% assign modules = site.documents | where: "collection", "opentelemetry" | where: "category", "Module 1: Foundations" | sort: "order" %}
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
