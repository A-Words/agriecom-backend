package net.awords.agriecombackend.repository;

import java.util.List;
import net.awords.agriecombackend.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update UserAddress ua set ua.defaultAddress = false where ua.user.id = :userId")
    void resetDefaultForUser(@Param("userId") Long userId);
}
