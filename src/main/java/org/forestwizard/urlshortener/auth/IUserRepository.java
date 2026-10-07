package org.forestwizard.urlshortener.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<AuthUser, String> {
    @Modifying
    @Query(
            value = "INSERT INTO url_shortener_db.auth_user(username, password_hash, role) " +
                    "VALUES(:username, :password_hash, :role)",
            nativeQuery = true
    )
    int insert(@Param("username") String username, @Param("password_hash") String password, @Param("role") String role);
    Optional<AuthUser> findByUsername(String username);
    boolean existsByUsername(String username);
}

