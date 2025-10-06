package net.awords.agriecombackend.repository;

import java.util.Optional;
import net.awords.agriecombackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
