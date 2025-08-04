package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.calendar.AuthorToGroupRequestDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupParticipantStatusDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupParticipantStatusSwitchDto;
import by.forward.forward_system.core.jpa.model.CalendarGroupEntity;
import by.forward.forward_system.core.jpa.model.CalendarGroupParticipantStatusEntity;
import by.forward.forward_system.core.jpa.model.CalendarGroupParticipantStatusEntityId;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.CalendarGroupParticipantStatusRepository;
import by.forward.forward_system.core.jpa.repository.CalendarGroupRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.mapper.CalendarGroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewCalendarService {

    private final CalendarGroupRepository calendarGroupRepository;
    private final CalendarGroupMapper calendarGroupMapper;
    private final UserRepository userRepository;
    private final CalendarGroupParticipantStatusRepository calendarGroupParticipantStatusRepository;

    @Transactional
    public CalendarGroupDto save(CalendarGroupDto request) {
        CalendarGroupEntity entity = calendarGroupMapper.map(request);
        entity = calendarGroupRepository.save(entity);
        entity.setCreatedAt(LocalDateTime.now());
        return calendarGroupMapper.map(entity);
    }

    @Transactional
    public void addUserToGroupRequest(AuthorToGroupRequestDto request) {
        Optional<UserEntity> user = userRepository.findById(request.getUserId());
        Optional<CalendarGroupEntity> calendarGroup = calendarGroupRepository.findById(request.getGroupId());
        calendarGroup.ifPresent(calendarGroupEntity -> {
            user.ifPresent(userEntity -> {
                calendarGroupEntity.getParticipants().add(userEntity);
            });
        });
    }

    @Transactional
    public void deleteFromGroup(AuthorToGroupRequestDto request) {
        Optional<UserEntity> user = userRepository.findById(request.getUserId());
        Optional<CalendarGroupEntity> calendarGroup = calendarGroupRepository.findById(request.getGroupId());
        calendarGroup.ifPresent(calendarGroupEntity -> {
            user.ifPresent(userEntity -> {
                calendarGroupEntity.getParticipants().remove(userEntity);
            });
        });
    }

    public CalendarGroupParticipantStatusDto getGroupParticipantsStatus(Long groupId, LocalDate begin, LocalDate end) {
        Set<CalendarGroupParticipantStatusEntity> group = calendarGroupParticipantStatusRepository.findGroupWorkStatusBetween(begin, end, groupId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        var dateToUserId = group.stream()
                .map(entity -> Map.entry(entity.getId().getDate(), entity.getId().getUserId()))
                .toList();

        var groupByDate = dateToUserId.stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().format(dateTimeFormatter)))
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().stream().map(Map.Entry::getValue).toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return CalendarGroupParticipantStatusDto.builder()
                .groupId(groupId)
                .days(groupByDate)
                .build();
    }

    public CalendarGroupParticipantStatusSwitchDto switchGroupParticipantStatus(Long groupId, Long userId, LocalDate date) {
        var id = new CalendarGroupParticipantStatusEntityId(userId, groupId, date);
        var dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Optional<CalendarGroupParticipantStatusEntity> byId = calendarGroupParticipantStatusRepository.findById(id);
        if (byId.isPresent()) {
            calendarGroupParticipantStatusRepository.deleteById(id);
            return CalendarGroupParticipantStatusSwitchDto.builder()
                    .userId(userId)
                    .groupId(groupId)
                    .isWorking(false)
                    .date(dateString)
                    .build();
        }


        CalendarGroupParticipantStatusEntity statusEntity = new CalendarGroupParticipantStatusEntity(id);
        calendarGroupParticipantStatusRepository.save(statusEntity);

        return CalendarGroupParticipantStatusSwitchDto.builder()
                .userId(userId)
                .groupId(groupId)
                .isWorking(true)
                .date(dateString)
                .build();
    }

    public List<CalendarGroupDto> getGroups() {
        return calendarGroupRepository.findAll().stream()
                .map(calendarGroupMapper::map)
                .sorted(Comparator.comparing(CalendarGroupDto::getName))
                .toList();
    }

    public Optional<CalendarGroupDto> getGroup(Long groupId) {
        return calendarGroupRepository.findById(groupId)
                .map(calendarGroupMapper::map);
    }

    @Transactional
    public void deleteGroup(CalendarGroupDto request) {
        calendarGroupParticipantStatusRepository.deleteByGroupId(request.getId());
        calendarGroupRepository.deleteById(request.getId());
    }
}
