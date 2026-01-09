---
layout: post
title: "GraphQL Federation for Java Backend Developers"
date: 2026-02-11
author: Laxman Sharma
image: /assets/images/graphql-federation-hero.png
categories: [java, api, microservices]
tags: [java, graphql, microservices, api-gateway, federation]
excerpt: "Build a unified GraphQL API layer over multiple Spring Boot microservices using Federation. Learn schema stitching, resolver composition, and query optimization."
---

# The API Gateway Problem

You have 5 microservices, each with its own REST API. Your frontend team builds a user dashboard that needs data from all 5. The result? **5 HTTP requests, waterfall loading, and  slow UX**.

GraphQL Federation solves this: **one query, one request, automatic data aggregation**.

---

![GraphQL Federation Architecture]({{ "" | relative_url }}/assets/images/graphql-federation-hero.png)

---

## Federation vs. Monolithic GraphQL

**Monolithic GraphQL**: One giant schema, one service.  
**Federation**: Each microservice owns part of the schema, a gateway stitches them together.

### Example: E-commerce System
```graphql
# User Service owns User
type User @key(fields: "id") {
  id: ID!
  email: String!
}

# Order Service extends User with orders
extend type User @key(fields: "id") {
  orders: [Order!]!
}
```

The gateway automatically fetches user data from User Service, then enriches it with orders from Order Service.

---

## Implementing with Spring GraphQL

### Subgraph (Microservice) Setup
```java
@Controller
public class UserController {
    
    @QueryMapping
    public User user(@Argument String id) {
        return userService.findById(id);
    }
    
    @SchemaMapping(typeName = "User")
    public List<Order> orders(User user) {
        // Called by Federation gateway to resolve orders
        return orderClient.getOrdersForUser(user.getId());
    }
}
```

### Schema Definition
```graphql
type Query {
  user(id: ID!): User
}

type User @key(fields: "id") {
  id: ID!
  email: String!
  orders: [Order!]!
}

type Order {
  id: ID!
  total: Float!
}
```

---

## The Federation Gateway

Use Apollo Gateway or Spring Cloud Gateway with GraphQL support:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: graphql-federation
          uri: http://gateway:4000
          predicates:
            - Path=/graphql
```

The gateway handles:
- **Query planning**: Determining which services to call
- **Data merging**: Combining responses
- **Caching**: Avoiding redundant requests

---

## N+1 Query Problem

GraphQL's biggest pitfall: **the N+1 problem**.

### Bad (N+1 queries):
```java
@SchemaMapping
public Order order(User user) {
    return orderService.findById(user.getOrderId());  // 1 query per user!
}
```

### Good (DataLoader batching):
```java
@Bean
public DataLoader<String, Order> orderLoader(BatchLoaderRegistry registry) {
    registry.forTypePair(String.class, Order.class)
        .registerBatchLoader((ids, env) -> 
            orderService.findAllById(ids)  // Single batched query
        );
    return DataLoaderFactory.newDataLoader(batchLoader);
}

@SchemaMapping
public CompletableFuture<Order> order(User user, DataLoader<String, Order> loader) {
    return loader.load(user.getOrderId());
}
```

---

## Security in Federation

Never expose internal microservice GraphQL endpoints. Only the gateway should be public.

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/graphql").hasRole("API_USER")
                .anyRequest().denyAll()
            )
            .build();
    }
}
```

---

## When NOT to Use Federation

| Scenario | Better Choice |
| :--- | :--- |
| **Services rarely need data from each other** | Separate REST APIs |
| **Real-time updates critical** | GraphQL Subscriptions + WebSockets |
| **Team unfamiliar with GraphQL** | Traditional API Gateway (Spring Cloud Gateway) |

---

## Conclusion

GraphQL Federation turns multiple microservices into a single, cohesive API without tight coupling. Your backend stays distributed, but your frontend gets a unified data graph.

*Building distributed systems? Check out [Sagas](#) for transactions or [Transactional Outbox]({{ "" | relative_url }}/2026/01/30/transactional-outbox-pattern/) for messaging.*
