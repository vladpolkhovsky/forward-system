package by.forward.forward_system.core.services.admin;

import by.forward.forward_system.core.dto.rest.admin.OrderRequestLogDto;
import by.forward.forward_system.core.jpa.model.OrderRequestStatisticEntity;
import by.forward.forward_system.core.jpa.repository.OrderRequestStatisticRepository;
import by.forward.forward_system.core.jpa.repository.OrderRequestStatisticRepository.IdProjection;
import by.forward.forward_system.core.jpa.repository.OrderRequestStatisticRepository.InfoProjection;
import by.forward.forward_system.core.jpa.specs.OrderRequestSpecification;
import by.forward.forward_system.core.mapper.OrderRequestStatisticMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderRequestService {

    private final OrderRequestStatisticRepository orderRequestStatisticRepository;
    private final OrderRequestStatisticMapper requestStatisticMapper;

    public Page<OrderRequestLogDto> find(Long managerId, Long authorId, String order, Pageable pageable) {
        Specification<OrderRequestStatisticEntity> specification = OrderRequestSpecification.findBy(managerId, authorId, order);

        Page<IdProjection> idsProjection = orderRequestStatisticRepository.findBy(specification, q -> q.project("id")
            .as(IdProjection.class).page(pageable));

        List<Long> ids = idsProjection.toList().stream()
            .map(IdProjection::getId)
            .toList();

        Map<Long, InfoProjection> idToProjection = orderRequestStatisticRepository.findByIdsProjection(ids).stream().collect(Collectors
            .toMap(InfoProjection::getId, Function.identity(), (a, b) -> a));

        return idsProjection.map(IdProjection::getId)
            .map(idToProjection::get)
            .map(requestStatisticMapper::map);
    }
}
