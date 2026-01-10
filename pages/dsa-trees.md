---
layout: page
title: "Module 4: Trees"
permalink: /dsa/trees/
---

# Tree Basics

A **Tree** is a hierarchical data structure consisting of nodes connected by edges. It starts with a single node called the **Root** and branches out to children nodes.

![Binary Tree Structure]({{ "/assets/images/dsa-tree-basics.png" | relative_url }})

## Vocabulary

- **Root**: The top-most node (no parent).
- **Leaf**: A node with no children.
- **Parent/Child**: A node is a parent if it has children.
- **Height**: Number of edges from the node to the deepest leaf.
- **Depth**: Number of edges from root to the node.

## Types of Trees

- **Binary Tree**: Each node has at most 2 children.
- **Binary Search Tree (BST)**: Left child < Parent < Right child.
- **AVL/Red-Black Tree**: Self-balancing BSTs.

---

# Problems

{% assign problems = site.dsa | where: "category", "Tree" | sort: "order" %}
{% for problem in problems %}
  <h3><a href="{{ problem.url | relative_url }}">{{ problem.order }}. {{ problem.title }}</a></h3>
  <p>{{ problem.excerpt }}</p>
{% endfor %}

*(No problems added yet)*
