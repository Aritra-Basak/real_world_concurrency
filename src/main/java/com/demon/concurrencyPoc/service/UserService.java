package com.demon.concurrencyPoc.service;

import com.demon.concurrencyPoc.dto.UserUpdateRequest;
import com.demon.concurrencyPoc.entity.User;
import com.demon.concurrencyPoc.exceptionHandling.customExceptions.ConcurrentUpdateException;
import com.demon.concurrencyPoc.exceptionHandling.customExceptions.UserNotFoundException;
import com.demon.concurrencyPoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Cacheable(cacheNames = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        simulateLatency(); // just to show effect with circuit breaker, etc.
        return userRepository.findById(id)
                .orElseThrow(() ->  new UserNotFoundException(id));
    }

    @CachePut(cacheNames = "users", key = "#id")
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            return userRepository.save(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException("User " + id + " was updated by another request", e);
        }
    }

    private void simulateLatency() {
        try {
            Thread.sleep(500); // 0.5 second for demo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Explanation:

// @Cacheable(cacheNames = "users", key = "#id")
// Caches the result of getUserById method.
// Subsequent calls with the same id will return the cached User object, improving performance.
// users is the name of the cache where User objects are stored, and #id is the key used to identify each cached User. (Defined in Cache Config in the configuration package)
/*
 With Caching:
*  GET /users/1
        ↓
   Cache hit (not found in Cache)→ DB → Cache store
        ↓
   GET /users/1 (again)
        ↓
    Cache hit (data found in Cache) → NO DB call*/

// @CachePut(cacheNames = "users", key = "#id")
// Updates the cache entry for the User object after it has been updated in the database (so the method update methods called first).
// Ensures that the cache always has the latest version of the User after an update operation.