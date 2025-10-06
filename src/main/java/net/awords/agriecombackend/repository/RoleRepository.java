package net.awords.agriecombackend.repository;

import java.util.Optional;
import net.awords.agriecombackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
