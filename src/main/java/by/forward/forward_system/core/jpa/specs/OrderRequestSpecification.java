package by.forward.forward_system.core.jpa.specs;

import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity_;
import by.forward.forward_system.core.jpa.model.OrderRequestStatisticEntity;
import by.forward.forward_system.core.jpa.model.OrderRequestStatisticEntity_;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderRequestSpecification {

    public static Specification<OrderRequestStatisticEntity> findBy(Long managerId, Long authorId, String order) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (managerId != null) {
                predicates.add(cb.equal(root.get(OrderRequestStatisticEntity_.MANAGER), managerId));
            }

            if (authorId != null) {
                predicates.add(cb.equal(root.get(OrderRequestStatisticEntity_.AUTHOR), authorId));
            }

            if (StringUtils.isNotBlank(order)) {
                String trimmedOrder = order.trim();

                Subquery<Long> orderSubquery = query.subquery(Long.class);
                Root<OrderEntity> orderRoot = orderSubquery.from(OrderEntity.class);
                orderSubquery.select(orderRoot.get(OrderEntity_.ID))
                    .where(cb.like(
                        cb.lower(orderRoot.get(OrderEntity_.TECH_NUMBER)),
                        "%" + trimmedOrder.toLowerCase() + "%"
                    ));

                predicates.add(root.get(OrderRequestStatisticEntity_.ORDER_ID).in(orderSubquery));
            }

            query.orderBy(cb.desc(root.get(OrderRequestStatisticEntity_.CREATED_AT)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
