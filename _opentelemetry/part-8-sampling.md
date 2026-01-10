---
layout: dsa_post
course: opentelemetry
title: "Part 8: Sampling & Scaling"
category: "Module 3: Production"
order: 8
date: 2026-01-14
author: Laxman Sharma
image: /assets/images/otel-part8-hero.png
categories: [observability, java]
tags: [opentelemetry, java, sampling, kubernetes, scaling]
excerpt: "Tail sampling, load balancing, and cost control."
---

# Sampling & Scaling: The Final Frontier

*Part 8 of an 8-part series on implementing observability in Java microservices*

---

Observability at scale has a dirty secret: **It's expensive.**

Logging every request, tracing every function, and counting every packet will bankrupt you. In this final module, we learn how to keep the signal but drop the noise.

## The Cost Equation

$$ Cost = Volume \times (Storage + Compute + Network) $$

In a system with 1,000 requests/sec, generating 10 spans per request:
*   10,000 spans/sec
*   ~5KB per span
*   **50MB/sec** -> **4.3TB/day**

You cannot store 4TB/day explicitly. You must **Sample**.

## Sampling Strategies

### 1. Head Sampling (The "Coin Flip")
Decision made at the **start** of the request (in the Java SDK).

*   **Logic**: "Keep 10% of traces."
*   **Pros**: Cheapest. Drop data before it leaves the app.
*   **Cons**: You might drop the *one* error trace you really needed.

```properties
# Java Agent Config
OTEL_TRACES_SAMPLER=parentbased_traceidratio
OTEL_TRACES_SAMPLER_ARG=0.1
```

### 2. Tail Sampling (The "Smart Way")
Decision made at the **end** of the trace (in the Collector).

*   **Logic**: "Keep 100% of Errors. Keep 100% of High Latency (>2s). Keep 1% of the rest."
*   **Pros**: You never miss an error.
*   **Cons**: Expensive. You must hold *all* spans in memory until the trace completes.

#### The Architecture Problem with Tail Sampling
For Tail Sampling to work, **all** spans for `TraceID: 123` must arrive at the **same** Collector instance so it can make a decision.

If you have 5 Replica Collectors behind a standard Round-Robin Load Balancer, spans will be scattered. The collector won't see the full trace.

#### The Solution: Load Balancing Exporter
You need a 2-Tier Collector Architecture.

1.  **Tier 1 (Gateway)**: Receives spans. Uses `loadbalancing` exporter to hash `TraceID` and send to Tier 2.
2.  **Tier 2 (Sampler)**: Receives ALL spans for a given TraceID. Makes the Keep/Drop decision.

```yaml
# Tier 1 Config (Load Balancer)
exporters:
  loadbalancing:
    protocol:
      port: 4317
    resolver:
      k8s: { service: "otel-collector-tier-2" }
```

## Production Checklist

Before you declare victory, verify these 5 items:

### 1. Unified Service Naming
Ensure `service.name` is consistent across Metrics, Logs, and Traces.
*   `OTEL_SERVICE_NAME=order-service`

### 2. Environment Tags
You will regret not knowing if a trace is from `prod` or `staging`.
*   `OTEL_RESOURCE_ATTRIBUTES=deployment.environment=production`

### 3. Queue Sizes
Monitor your Collector's queue size. If it's always full, you are dropping data. Increase `send_batch_size` or add replicas.

### 4. Health Checks
Don't trace your Kubernetes Health Checks (`/health`, `/readiness`). They are spam.
*   **Java Agent 2.0+**: `OTEL_JAVAAGENT_EXCLUDE_CLASSES` or filter via Collector.

### 5. Secure your Exporters
Never expose `0.0.0.0:4317` to the public internet. Use internal ClusterIPs or mTLS.

## Conclusion

We have built a world-class Observability stack. We started with **Java Agents**, moved to **Manual Spans**, added **Baggage**, integrated **Metrics & Logs**, built a **Collector Pipeline**, and optimized it with **Tail Sampling**.

You are now ready to debug anything.
