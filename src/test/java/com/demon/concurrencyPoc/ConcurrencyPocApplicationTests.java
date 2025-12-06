package com.demon.concurrencyPoc;

import com.demon.concurrencyPoc.dto.UserUpdateRequest;
import com.demon.concurrencyPoc.entity.User;
import com.demon.concurrencyPoc.exceptionHandling.customExceptions.ConcurrentUpdateException;
import com.demon.concurrencyPoc.repository.UserRepository;
import com.demon.concurrencyPoc.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ConcurrencyPocApplicationTests {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public ConcurrencyPocApplicationTests(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // This is a pure DB concurrency test.
    @Test
    void concurrentUpdates_shouldResultInSingleSuccessAndConflicts() throws Exception {
        // 1. Create a user
        User user = User.builder()
                .username("concurrent-user")
                .email("original@test.com")
                .phone("1111111111")
                .build();
        user = userRepository.saveAndFlush(user);
        final Long userId = user.getId();
        int threadCount = 10;
        // Latches are being used to coordinate thread start and end
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);   // using ExecutorService to run tasks in parallel.
        CountDownLatch ready = new CountDownLatch(threadCount);                // Ensures ALL threads are ready
        CountDownLatch start = new CountDownLatch(1);                         // Releases all threads simultaneously
        CountDownLatch done = new CountDownLatch(threadCount);               // Waits until ALL threads finish

        List<Future<Boolean>> futures = new ArrayList<>();

        //Each iteration creates one task that will run in a separate thread.
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {            // submitting the task to executor and adding the return values from each thread to futures list
                ready.countDown();                        // tells main thread we're ready inside the task
                start.await();                           // wait until all threads are ready inside the task

                try {
                    UserUpdateRequest req = new UserUpdateRequest();
                    // Each thread tries to set a unique email and phone over a same user
                    req.setEmail("email" + idx + "@test.com");
                    req.setPhone("99999999" + idx);

                    userService.updateUser(userId, req);
                    return true;
                } catch (ConcurrentUpdateException | ObjectOptimisticLockingFailureException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    done.countDown(); // tell main thread we're done
                }
            }));
        }

        // Main thread coordination:
        // Allow all threads to start "at the same time"
        ready.await();      // wait until all are prepared
        start.countDown();  // fire!
        done.await();       // wait until all finish
        executor.shutdown();

        long successCount = 0;
        long failureCount = 0;

        for (Future<Boolean> f : futures) {
            if (f.get()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        // With optimistic locking we expect exactly one success and the rest failures
        assertEquals(1L, successCount, "Exactly one thread should succeed");
        assertTrue(failureCount >= 1, "At least one thread should fail due to optimistic locking");

        // Check final entity state
        User finalUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found after test"));
        System.out.println(" Success Count = " + successCount);
        System.out.println(" Failure Count = " + failureCount);
        System.out.println("Final email in DB: " + finalUser.getEmail());
        System.out.println("Final version: " + finalUser.getVersion());
    }

}
