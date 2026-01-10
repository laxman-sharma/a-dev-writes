---
layout: dsa_post
title: "Module 1: Arrays"
permalink: /dsa/array/
category: Array
---

# Array Basics

An **Array** is a linear data structure that collects elements of the same data type and stores them in contiguous and adjacent memory locations. Arrays work on an index system starting from 0 to (n-1), where `n` is the size of the array.

![Array Memory Layout]({{ "/assets/images/dsa-array-basics.png" | relative_url }})

## Key Characteristics

- **Fixed Size**: Once declared, the size of an array cannot be changed (in standard static arrays).
- **Random Access**: You can access any element in `O(1)` time using its index.
- **Memory**: Elements are stored sequentially in memory.

## Time Complexity

| Operation | Complexity |
| :--- | :--- |
| Access | **O(1)** |
| Search (Unsorted) | **O(n)** |
| Search (Sorted) | **O(log n)** |
| Insertion | **O(n)** (due to shifting) |
| Deletion | **O(n)** (due to shifting) |

---

# Problems

{% assign problems = site.dsa | where: "category", "Array" | sort: "order" %}
{% for problem in problems %}
  <h3><a href="{{ problem.url | relative_url }}">{{ problem.order }}. {{ problem.title }}</a></h3>
  <p>{{ problem.excerpt }}</p>
{% endfor %}
