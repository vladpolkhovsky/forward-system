package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.PlanEntity;
import by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {

    @Query(value = """
            select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p
                    inner join UserEntity u on p.userId = u.id
                    where p.userId = :userId order by p.start
        """)
    List<UserPlanProjectionDto> loadUserPlans(Long userId);

    @Query(value = """
            select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p
                    inner join UserEntity u on p.userId = u.id
                    where p.userId in :userIds order by p.start limit 100
        """)
    List<UserPlanProjectionDto> loadUsersPlans(List<Long> userIds);

    @Query(value = """
            select new by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto(p, u) from PlanEntity p
                    inner join UserEntity u on p.userId = u.id
                    where p.userId = :userId and p.start <= :now and :now <= p.end order by p.start
        """)
    List<UserPlanProjectionDto> findAllUserPlans(Long userId, LocalDateTime now);

    @Query(nativeQuery = true, value = """
            select
                up.id as planId,
                up.plan_start as planStart,
                up.plan_end as planEnd,
                now() < up.plan_start as isPlanNotStartYet,
                up.plan_start <= now() and now() <= up.plan_end as isPlanActive,
                extract(day from now() - up.plan_start) as beforePlanBeginDays,
                extract(hour from now() - up.plan_start) as beforePlanBeginHours,
                u.id as userId,
                u.username as username,
                up.target_sum as targetSum,
                coalesce(sum(o.taking_cost), 0.0) as orderSum,
                case when up.target_sum = 0 then 0 else coalesce(sum(o.taking_cost), 0.0) / up.target_sum end as orderSumPercent,
                up.target_count as targetCount,
                coalesce(count(o), 0.0) as orderCountSum,
                case when up.target_count = 0 then 0 else coalesce(count(o), 0.0) / up.target_count end as orderCountSumPercent
            from
                forward_system.user_plan up
            inner join forward_system.users u on
                up.user_id = u.id
            left join forward_system.order_participants op on
                op.user_id = u.id
            left join forward_system.orders o on
                op.order_id = o.id and o.created_at between up.plan_start and up.plan_end
            where
                up.plan_start > now() - interval '9 month'
            group by
                up.id, u.id
            order by
                u.username, up.plan_start desc
        """)
    List<UserPlanProgressProjection> findAllUserPlanView();

    @Query(nativeQuery = true, value = """
            select
                up.id as planId,
                up.plan_start as planStart,
                up.plan_end as planEnd,
                now() < up.plan_start as isPlanNotStartYet,
                up.plan_start <= now() and now() <= up.plan_end as isPlanActive,
                extract(day from now() - up.plan_start) as beforePlanBeginDays,
                extract(hour from now() - up.plan_start) as beforePlanBeginHours,
                u.id as userId,
                u.username as username,
                up.target_sum as targetSum,
                coalesce(sum(o.taking_cost), 0.0) as orderSum,
                case when target_sum = 0 then 0 else coalesce(sum(o.taking_cost), 0.0) / up.target_sum end as orderSumPercent,
                up.target_count as targetCount,
                coalesce(count(o), 0.0) as orderCountSum,
                case when up.target_count = 0 then 0 else coalesce(count(o), 0.0) / up.target_count end as orderCountSumPercent
            from
                forward_system.user_plan up
            inner join forward_system.users u on
                up.user_id = u.id
            left join forward_system.order_participants op on
                op.user_id = u.id
            left join forward_system.orders o on
                op.order_id = o.id and o.created_at between up.plan_start and up.plan_end
            where
                up.plan_start > now() - interval '9 month'
                and now() between up.plan_start and up.plan_end
                and up.user_id = :userId
            group by
                up.id, u.id
            order by
                u.username, up.plan_start desc
            limit 1
        """)
    Optional<UserPlanProgressProjection> finActivePlanByUser(Long userId);

    interface UserPlanProgressProjection {
        Long getPlanId();
        LocalDateTime getPlanStart();
        LocalDateTime getPlanEnd();
        Boolean getIsPlanNotStartYet();
        Boolean getIsPlanActive();
        Long getBeforePlanBeginDays();
        Long getBeforePlanBeginHours();
        Long getUserId();
        String getUsername();
        Double getTargetSum();
        Double getOrderSum();
        Double getOrderSumPercent();
        Double getTargetCount();
        Double getOrderCountSum();
        Double getOrderCountSumPercent();
    }
}
