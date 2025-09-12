package by.forward.forward_system.core.jpa.specs;

import by.forward.forward_system.core.dto.rest.search.OrderSearchCriteria;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity_;
import by.forward.forward_system.core.jpa.model.OrderStatusEntity_;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class OrderSpecification {

    public static Specification<OrderEntity> buildSearchIdsSpecification(OrderSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = Stream.of(
                    Optional.ofNullable(criteria.getTechNumber()).map(techNumber ->
                        cb.like(root.get(OrderEntity_.techNumber), '%' + techNumber + '%')),
                    Optional.ofNullable(criteria.getShowClosed()).filter(BooleanUtils::isFalse).map(ignored ->
                        cb.notEqual(root.get(OrderEntity_.orderStatus).get(OrderStatusEntity_.name), OrderStatus.CLOSED.getName())),
                    Optional.ofNullable(criteria.getStatus()).map(status ->
                        cb.equal(root.get(OrderEntity_.orderStatus).get(OrderStatusEntity_.name), status))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

            Expression<Integer> byTechNumber = root.get(OrderEntity_.techNumber).as(Integer.class);

            query.orderBy(cb.desc(byTechNumber));

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}
