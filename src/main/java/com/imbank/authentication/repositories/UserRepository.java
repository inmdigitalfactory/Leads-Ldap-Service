package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstByUsername(String username);

    Optional<User> findFirstByUsernameAndEnabled(String username, boolean enabled);

//    @Query(nativeQuery = true, value = "select")
//    Optional<User> findFirstByUsernameAndApp(String username, Long id);
}
