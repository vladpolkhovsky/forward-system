package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByRolesContains(String roleName);

    List<UserEntity> findByRolesNotContains(String roleName);

}
