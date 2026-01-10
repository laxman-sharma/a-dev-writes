---
layout: page
title: "Module 3: Linked Lists"
permalink: /dsa/linked-list/
---

# Linked List Basics

A **Linked List** is a linear data structure where elements are not stored in contiguous memory locations. Instead, each element (node) points to the next using a pointer or reference.

![Linked List Anatomy]({{ "/assets/images/dsa-linkedlist-basics.png" | relative_url }})

## Types of Linked Lists

1.  **Singly Linked List**: Each node points to the next node. Traversal is one-way.
2.  **Doubly Linked List**: Each node has two pointers: `next` and `prev`. Traversal is two-way.
3.  **Circular Linked List**: The last node points back to the head.

## Time Complexity

| Operation | Complexity | Note |
| :--- | :--- | :--- |
| Access | **O(n)** | Must traverse from head |
| Insertion (at Head) | **O(1)** | Just update pointers |
| Insertion (at End) | **O(n)** | Unless you have a tail pointer |
| Deletion (at Head) | **O(1)** | |

---

# Problems

{% assign problems = site.dsa | where: "category", "LinkedList" | sort: "order" %}
{% for problem in problems %}
  <h3><a href="{{ problem.url | relative_url }}">{{ problem.order }}. {{ problem.title }}</a></h3>
  <p>{{ problem.excerpt }}</p>
{% endfor %}

*(No problems added yet)*
