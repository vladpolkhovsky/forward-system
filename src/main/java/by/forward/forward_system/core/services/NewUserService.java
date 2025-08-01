package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserDto getUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id).get();
        return userMapper.map(userEntity);
    }
}
