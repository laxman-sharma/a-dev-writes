package com.adev.structured;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

@RestController
public class AggregationController {

    @GetMapping("/user/{id}/dashboard")
    public DashboardData getDashboard(@PathVariable String id) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork three parallel tasks
            var userTask = scope.fork(() -> fetchUser(id));
            var ordersTask = scope.fork(() -> fetchOrders(id));
            var recommendationsTask = scope.fork(() -> fetchRecommendations(id));

            // Wait for all to complete (or first failure)
            scope.join();
            scope.throwIfFailed();

            // Aggregate results
            return new DashboardData(
                userTask.get(),
                ordersTask.get(),
                recommendationsTask.get()
            );
        }
    }

    @GetMapping("/user/{id}/dashboard-traditional")
    public DashboardData getDashboardTraditional(@PathVariable String id) throws InterruptedException {
        // Traditional approach: sequential calls (slow!)
        var user = fetchUser(id);
        var orders = fetchOrders(id);
        var recommendations = fetchRecommendations(id);
        return new DashboardData(user, orders, recommendations);
    }

    // Simulated API calls
    private String fetchUser(String id) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(200));
        if (id.equals("error")) throw new RuntimeException("User service down!");
        return "User{id=" + id + ", name='John Doe'}";
    }

    private String fetchOrders(String id) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(300));
        return "Orders{count=5}";
    }

    private String fetchRecommendations(String id) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(250));
        return "Recommendations{products=[A, B, C]}";
    }

    record DashboardData(String user, String orders, String recommendations) {}
}
