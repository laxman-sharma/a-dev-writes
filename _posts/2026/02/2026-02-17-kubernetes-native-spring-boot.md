---
layout: post
title: "The Developer Platform: Kubernetes-Native Spring Boot"
date: 2026-02-17
author: Laxman Sharma
image: /assets/images/kubernetes-platform-hero.png
categories: [java, kubernetes, platform-engineering]
tags: [java, kubernetes, platform-engineering, operators, spring-boot]
excerpt: "Build a custom Internal Developer Platform (IDP) using Kubernetes Operators and Spring Boot. Learn how to automate infrastructure provisioning with CRDs."
---

# Platform Engineering: The New DevOps

Developers shouldn't need to understand Kubernetes YAML to deploy their apps. **Platform Engineering** creates self-service abstractions that hide complexity.

The goal: `git push` → deployed to production, with zero manual infrastructure work.

---

![Kubernetes Platform Architecture]({{ "" | relative_url }}/assets/images/kubernetes-platform-hero.png)

---

## Custom Resource Definitions (CRDs)

CRDs let you extend Kubernetes with your own resource types.

### Example: SpringBootApp CRD
```yaml
apiVersion: platform.example.com/v1
kind: SpringBootApp
metadata:
  name: my-app
spec:
  image: myregistry/app:v1.0
  replicas: 3
  database:
    type: postgres
    size: small
  cache:
    enabled: true
```

This single CRD automatically provisions:
- Deployment + Service
- PostgreSQL database
- Redis cache
- Ingress with TLS

---

## Building a Kubernetes Operator

Operators watch CRDs and reconcile desired state.

### Spring Boot Operator (Java K8s Client)
```java
@Component
public class SpringBootAppReconciler {
    private final KubernetesClient client;
    
    @Scheduled(fixedDelay = 10000)
    public void reconcile() {
        var apps = client.resources(SpringBootApp.class).list().getItems();
        
        for (var app : apps) {
            ensureDeployment(app);
            ensureDatabase(app);
            ensureIngress(app);
        }
    }
    
    private void ensure Deployment(SpringBootApp app) {
        var deployment = new DeploymentBuilder()
            .withNewMetadata()
                .withName(app.getMetadata().getName())
            .endMetadata()
            .withNewSpec()
                .withReplicas(app.getSpec().getReplicas())
                .withNewTemplate()
                    .withNewSpec()
                        .addNewContainer()
                            .withImage(app.getSpec().getImage())
                            .withName("app")
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
            
        client.resource(deployment).createOrReplace();
    }
}
```

---

## GitOps Integration

Connect your operator to Git for declarative infrastructure:

```java
@Component
public class GitOpsController {
    
    @Scheduled(fixedDelay = 60000)
    public void syncFromGit() {
        var manifests = gitRepo.getManifests();
        
        for (var manifest : manifests) {
            var app = parser.parse(manifest, SpringBootApp.class);
            client.resource(app).createOrReplace();
        }
    }
}
```

Now your infrastructure is **code-reviewed, versioned, and auditable**.

---

## Self-Service Developer Experience

Developers interact via simple commands:

```bash
# Create an app
kubectl apply -f myapp.yaml

# Scale
kubectl patch springbootapp my-app -p '{"spec":{"replicas":5}}'

# View status
kubectl get springbootapp my-app
```

The operator handles all the Kubernetes complexity.

---

## Production Patterns

### 1. Status Reporting
Update CRD status so developers see deployment progress:

```java
app.getStatus().setPhase("Provisioning");
app.getStatus().setDatabaseReady(false);
client.resource(app).updateStatus();
```

### 2. Garbage Collection
Use owner references for automatic cleanup:

```java
deployment.getMetadata().setOwnerReferences(List.of(
    new OwnerReferenceBuilder()
        .withApiVersion(app.getApiVersion())
        .withKind(app.getKind())
        .withName(app.getMetadata().getName())
        .withUid(app.getMetadata().getUid())
        .withController(true)
        .build()
));
```

When the CRD is deleted, Kubernetes auto-deletes owned resources.

---

## When to Build a Platform

| Signal | Action |
| :--- | :--- |
| **Developers copy-paste K8s YAML** | Build a platform |
| **Onboarding new services takes days** | Build a platform |
| **Configuration drift between envs** | Build a platform |
| **< 10 services total** | Wait, use Helm charts |

---

## Conclusion

Kubernetes-native platforms turn infrastructure into code. By building operators with Spring Boot, you leverage familiar Java patterns while providing developers with a self-service experience that scales.

*Building distributed systems? Check out [Saga Pattern]({{ "" | relative_url }}/2026/02/14/saga-pattern-microservices/) or [GraphQL Federation]({{ "" | relative_url }}/2026/02/11/graphql-federation-java/).*
