---
layout: page
title: "Module 2: Strings"
permalink: /dsa/strings/
---

# String Basics

A **String** is a sequence of characters. In many languages like Java and Python, strings are immutable, meaning they cannot be changed once created.

![String Immutability and Constant Pool]({{ "/assets/images/dsa-string-basics.png" | relative_url }})

## Key Concepts

- **Immutability**: Changing a string creates a new object in memory.
- **String Constant Pool** (Java): A special memory region to save space by sharing identical string literals.
- **StringBuilder/StringBuffer**: Mutable alternatives for efficient string manipulation.

## Common Operations

| Operation | Complexity |
| :--- | :--- |
| Concatenation (Immutable) | **O(n)** |
| Substring | **O(n)** |
| Char Access | **O(1)** |
| String Matching | **O(n*m)** (Naive) or **O(n)** (KMP) |

---

# Problems

{% assign problems = site.dsa | where: "category", "String" | sort: "order" %}
{% for problem in problems %}
  <h3><a href="{{ problem.url | relative_url }}">{{ problem.order }}. {{ problem.title }}</a></h3>
  <p>{{ problem.excerpt }}</p>
{% endfor %}

*(No problems added yet)*
