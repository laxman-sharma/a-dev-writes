---
layout: page
title: "Module 5: Graphs"
permalink: /dsa/graphs/
---

# Graph Basics

A **Graph** is a non-linear data structure consisting of **Vertices** (nodes) and **Edges** (connections between vertices). It is the most general structure used to model networks (social networks, maps, internet).

![Graph Types]({{ "/assets/images/dsa-graph-basics.png" | relative_url }})

## Types of Graphs

- **Directed (Digraph)**: Edges have a direction (A → B).
- **Undirected**: Edges have no direction (A — B).
- **Weighted**: Edges have values/weights (useful for shortest path).
- **Cyclic/Acyclic**: Whether the graph contains loops.

## Representing Graphs

1.  **Adjacency Matrix**: A 2D array. `matrix[i][j] = 1` if edge exists. Good for dense graphs. O(1) lookup.
2.  **Adjacency List**: Array of Lists. `adj[i]` contains all neighbors of `i`. Good for sparse graphs. O(V+E) traversal.

---

# Problems

{% assign problems = site.dsa | where: "category", "Graph" | sort: "order" %}
{% for problem in problems %}
  <h3><a href="{{ problem.url | relative_url }}">{{ problem.order }}. {{ problem.title }}</a></h3>
  <p>{{ problem.excerpt }}</p>
{% endfor %}

*(No problems added yet)*
