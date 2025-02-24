package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.table.UserTableViewDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class UserTableViewService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public List<UserTableViewDto> get(String col, String order) {
        Comparator<UserTableViewDto> comparator = Comparator.comparing((a) -> a.getUsername().toLowerCase());

        if (col.equals("activeOrders")) {
            comparator = Comparator.comparing(UserTableViewDto::activeOrderCount);
        }

        if (order.equals("desc")) {
            comparator = comparator.reversed();
        }

        return userRepository.findAllByDeletedIsFalse().stream()
            .filter(t -> !t.hasAuthority(Authority.AUTHOR))
            .map(this::toViewDto)
            .sorted(comparator)
            .toList();
    }

    private UserTableViewDto toViewDto(UserEntity userEntity) {
        String username = userEntity.getUsername();
        UserTableViewDto userTableViewDto = new UserTableViewDto(username);

        List<String> allTechNumberByParticipant = orderRepository.getAllTechNumberByParticipant(
            userEntity.getId(),
            Arrays.asList(ParticipantType.CATCHER.getName(), ParticipantType.HOST.getName()),
            Arrays.asList(OrderStatus.CLOSED.getName())
        );

        userEntity.getAuthorities().forEach(userTableViewDto::addAuthority);

        userTableViewDto.orderTechNumbers().addAll(allTechNumberByParticipant);
        userTableViewDto.orderTechNumbers().sort(String::compareTo);

        return userTableViewDto;
    }

}
