package com.demon.concurrencyPoc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String phone;

    // For optimistic locking: This @Version field will cause Hibernate to include the version in WHERE on update.
    // If two updates happen concurrently, one will fail with OptimisticLockException.
    // The row matches (version = 0), so update succeeds.
    // Then DB sets version = 1
    // Their UPDATE still says WHERE version = 0.
    // But the DB row now has version = 1.
    // So 0 rows are updated.
    // Hibernate sees this and throws ObjectOptimisticLockingFailureException.
    @Version // @Version behind the scene does (1 + user.getVersion()); to perform optimistic locking.
    private Long version;
}