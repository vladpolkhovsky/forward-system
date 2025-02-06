package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsernameAndDeletedIsFalse(String username);

    List<UserEntity> findByRolesContainsAndDeletedIsFalse(String roleName);

    List<UserEntity> findByRolesNotContainsAndDeletedIsFalse(String roleName);

    @Query(nativeQuery = true, value = "select * from forward_system.users u where u.roles not like '%AUTHOR%' and not u.is_deleted")
    List<UserEntity> getUsersFast();

    @Query(nativeQuery = true, value = "select u.username from forward_system.bot_integration_data bid inner join forward_system.users u on bid.user_id = u.id where not u.is_deleted order by u.username")
    List<String> findAllUsernamesRegisteredInBot();
}
