package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.PaymentEntity;
import by.forward.forward_system.core.jpa.repository.projections.PaymentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query(nativeQuery = true, value = """
        select
        	p.id as id,
        	p.payment_number as number,
        	p.status as statusName,
        	u.username as username,
        	p.created_at as created,
        	p.updated_at as updated
        from
        	forward_system.payment p
        inner join forward_system.users u on
        	p.user_id = u.id
        order by
        	p.updated_at desc
        limit :limit
        offset :offset
        """)
    List<PaymentDto> getAllPaymentPage(long offset, long limit);

    @Query(nativeQuery = true, value = """
        select
        	p.id as id,
        	p.payment_number as number,
        	p.status as statusName,
        	u.username as username,
        	p.created_at as created,
        	p.updated_at as updated
        from
        	forward_system.payment p
        inner join forward_system.users u on
        	p.user_id = u.id
        where
            p.user_id = :userId
        order by
        	p.updated_at desc
        limit :limit
        offset :offset
        """)
    List<PaymentDto> getUserPaymentPage(long userId, long offset, long limit);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.payment where user_id = :userId")
    long countByUser(long userId);

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.payment where user_id = :userId and id = :paymentId")
    boolean paymentExists(Long paymentId, Long userId);

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.payment where status = :status and id = :paymentId")
    boolean isPaymentWithStatus(Long paymentId, String status);

    @Query(nativeQuery = true, value = "select status from forward_system.payment where id = :paymentId")
    Optional<String> getPaymentStatus(Long paymentId);
}
