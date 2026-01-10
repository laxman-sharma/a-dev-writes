---
layout: dsa_post
course: opentelemetry
title: "Module 3: Production"
category: "Module 3: Production"
permalink: /opentelemetry/module-3-production/
---
# Module 3: Production Operations

Operationalize your observability. Learn how to handle data at scale and manage the OpenTelemetry Collector.

{% assign modules = site.documents | where: "collection", "opentelemetry" | where: "category", "Module 3: Production" | sort: "order" %}
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
    {% endfor %}</div>
