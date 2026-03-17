package com.example.platform.modules.users.repository;

import com.example.platform.modules.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    /**
     * Find user by email with roles and permissions eagerly loaded.
     * This prevents LazyInitializationException when accessing permissions
     * outside the transaction (e.g., in UserPrincipal.fromEntity).
     */
    @Query("SELECT DISTINCT u FROM UserEntity u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String email);

    boolean existsByEmail(String email);
}

