package com.demon.concurrencyPoc.service.faultTolerance;

import com.demon.concurrencyPoc.dto.UserUpdateRequest;
import com.demon.concurrencyPoc.entity.User;
import com.demon.concurrencyPoc.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResilientUserFacade {

    private final UserService userService;

    @Autowired
    public ResilientUserFacade(UserService userService) {
        this.userService = userService;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @RateLimiter(name = "userServiceGet")
    @Bulkhead(name = "userServiceGet")
    public User getUser(Long id) {
        return userService.getUserById(id);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    @RateLimiter(name = "userServiceUpdate")
    @Bulkhead(name = "userServiceUpdate")
    @Retry(name = "userServiceUpdate")
    public User updateUser(Long id, UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    // Fallbacks
    private User getUserFallback(Long id, Throwable ex) {
        throw new RuntimeException("User service currently unavailable. Reason: " + ex.getMessage());
    }

    private User updateUserFallback(Long id, UserUpdateRequest request, Throwable ex) {
        throw new RuntimeException("Unable to update user at the moment. Reason: " + ex.getMessage());
    }
}

// Explanation:

// @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
// Prevents cascading failures.
// If a downstream call keeps failing:
// Don’t keep hitting it
// Fail fast
// Give it time to recover
/*
*   What happens at runtime
        While CLOSED:
            Calls go through normally
        If failure rate exceeds threshold (e.g., 50%):
            Circuit becomes OPEN
        While OPEN:
            Calls do not invoke userService.updateUser()
            Method immediately fails or goes to fallback
        After wait duration:
            Circuit goes to HALF_OPEN
        Allows limited calls
            If successful → CLOSED again
            If failures → OPEN again
* */

// @RateLimiter(name = "userServiceUpdate")
// Controls the rate of calls to prevent overwhelming the service.
// E.g., max 10 calls per second
// userServiceUpdate is the config name defined in application.yml -- resilience4j.ratelimiter.instances.userServiceUpdate

// @Bulkhead(name = "userServiceUpdate")
// Limits the number of concurrent calls to avoid resource exhaustion.
// E.g., max 5 concurrent calls
// userServiceUpdate is the config name defined in application.yml -- resilience4j.bulkhead.instances.userServiceUpdate

// @Retry(name = "userServiceUpdate")
// Automatically retries failed calls based on configured policies.
// E.g., retry up to 3 times with a fixed delay
// userServiceUpdate is the config name defined in application.yml -- resilience4j.retry.instances.userServiceUpdate