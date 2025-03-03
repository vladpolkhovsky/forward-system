package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.PlanEntity;
import by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {

    @Query(value = "select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p inner join UserEntity u on p.userId = u.id where p.userId = :userId order by p.start")
    List<UserPlanProjectionDto> loadUserPlans(Long userId);

    @Query(value = "select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p inner join UserEntity u on p.userId = u.id where p.userId in :userIds order by p.start")
    List<UserPlanProjectionDto> loadUsersPlans(List<Long> userIds);

    @Query(value = "select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p inner join UserEntity u on p.userId = u.id where p.userId = :userId and p.start <= :now and :now <= p.end order by p.start")
    List<UserPlanProjectionDto> findAllUserPlans(Long userId, LocalDateTime now);
}
