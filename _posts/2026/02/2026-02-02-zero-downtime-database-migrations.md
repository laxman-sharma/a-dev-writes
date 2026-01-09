---
layout: post
title: "Zero-Downtime Database Migrations with Liquibase"
date: 2026-02-02
author: Laxman Sharma
image: /assets/images/db-migrations-hero.png
categories: [java, devops, database]
tags: [java, liquibase, database, migrations, devops, blue-green]
excerpt: "Learn how to handle schema changes in production without downtime using Liquibase and the expand-contract pattern for blue-green deployments."
---

# The Production Migration Dilemma

You're deploying a new version of your microservice that renames a database column from `user_name` to `username`. Sounds simple, right?

**Wrong.** If you deploy the new code and run the migration simultaneously, you'll have a brief window where:
- Old pods are still running with code expecting `user_name`
- The database now only has `username`
- **Result**: Production outage

This is the **zero-downtime migration problem**.

---

![Blue-Green Database Deployment]({{ "" | relative_url }}/assets/images/db-migrations-hero.png)

---

## The Expand-Contract Pattern

The solution is a **three-phase migration** strategy:

### Phase 1: Expand (Add New Column)
Add the new column alongside the old one. Both versions of your app can run simultaneously.

```xml
<changeSet id="1" author="dev">
    <addColumn tableName="users">
        <column name="username" type="VARCHAR(255)"/>
    </addColumn>
    <sql>UPDATE users SET username = user_name WHERE username IS NULL;</sql>
</changeSet>
```

### Phase 2: Migrate Application
Deploy your new application version that writes to **both** columns but reads from the new one.

```java
@Entity
public class User {
    @Column(name = "username")
    private String username;  // New

    @Column(name = "user_name")
    @Deprecated
    private String userNameLegacy;  // Keep for compatibility

    @PrePersist
    @PreUpdate
    void sync() {
        this.userNameLegacy = this.username;  // Dual-write
    }
}
```

### Phase 3: Contract (Remove Old Column)
After all instances are updated, drop the old column in a later release.

```xml
<changeSet id="2" author="dev">
    <dropColumn tableName="users" columnName="user_name"/>
</changeSet>
```

---

## Liquibase Best Practices

### 1. Never Edit Existing Changesets
Once a changeset runs in production, it's immutable. Always create new changesets.

### 2. Use Preconditions
Prevent destructive operations in production:

```xml
<changeSet id="3" author="dev">
    <preConditions onFail="MARK_RAN">
        <not><columnExists tableName="users" columnName="username"/></not>
    </preConditions>
    <addColumn tableName="users">
        <column name="username" type="VARCHAR(255)"/>
    </addColumn>
</changeSet>
```

### 3. Test Rollbacks
Always verify your `<rollback>` tags work:

```xml
<changeSet id="4" author="dev">
    <addColumn tableName="orders" columnName="total_amount" type="DECIMAL(10,2)"/>
    <rollback>
        <dropColumn tableName="orders" columnName="total_amount"/>
    </rollback>
</changeSet>
```

---

## Integration with CI/CD

### Kubernetes Init Container Pattern
Run migrations before app deployment:

```yaml
initContainers:
  - name: liquibase-migrator
    image: liquibase/liquibase:latest
    command: ["liquibase", "update"]
    env:
      - name: LIQUIBASE_URL
        value: "jdbc:postgresql://db:5432/mydb"
```

This ensures migrations complete before traffic hits the new pods.

---

## Common Pitfalls

| Mistake | Impact | Solution |
| :--- | :--- | :--- |
| **Renaming columns directly** | Breaks old app instances | Use expand-contract |
| **Adding NOT NULL immediately** | Fails for existing rows | Add column as nullable first, backfill, then add constraint |
| **Large data migrations in changesets** | Locks tables | Use batched updates in application code |

---

## Conclusion

Zero-downtime migrations aren't optional for production systems—they're a requirement. By combining Liquibase's tracking with the expand-contract pattern, you can evolve your schema safely while your application continues serving traffic.

*Need more resilience patterns? Check out [Transactional Outbox]({{ "" | relative_url }}/2026/01/30/transactional-outbox-pattern/) or [Saga Pattern](#) for distributed transactions.*
