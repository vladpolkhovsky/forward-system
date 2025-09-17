package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.rest.UserPlanViewProjectionDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.PlanEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.PlanRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.UserSimpleProjectionDto;
import by.forward.forward_system.core.mapper.UserPlanMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PlanService {

    private final UserRepository userRepository;

    private final PlanRepository planRepository;

    private final OrderRepository orderRepository;
    private final UserPlanMapper userPlanMapper;

    public List<UserSimpleProjectionDto> getAllManagers() {
        return userRepository.loadAllWithRole(Authority.MANAGER);
    }

    public List<UserPlanProjectionDto> getAllUserPlans(Long userId) {
        return planRepository.loadUserPlans(userId);
    }

    public List<UserPlanProjectionDto> getAllUsersPlans(List<Long> userId) {
        return planRepository.loadUsersPlans(userId);
    }

    public List<UserPlanDetailsDto> getUserCurrentPlan(Long userId) {
        return planRepository.findAllUserPlans(userId, LocalDateTime.now()).stream()
            .map(this::userPlanToDetails)
            .toList();
    }

    public void savePlan(Long targetUserId, LocalDateTime planStart, LocalDateTime planEnd, Long planTargetSum, Long planTargetCount, Long createdBy) {
        planRepository.save(new PlanEntity(null, targetUserId, createdBy, planTargetSum, planTargetCount, planStart, planEnd, LocalDateTime.now()));
    }

    public ValidationResponse validate(ValidationRequest validationRequest) {
        return new ValidationResponse(true, null);
    }

    public UserPlanDetailsDto userPlanToDetails(UserPlanProjectionDto dto) {
        Long sum = orderRepository.getOrdersSumByTimeAndCatcher(dto.getUserId(), dto.getStartDateTime(), dto.getEndDateTime());
        return new UserPlanDetailsDto(
            dto.getId(),
            dto.getPlanName(),
            Math.min(Math.round((1.0 * sum) / dto.getTargetSum() * 100), 100),
            dto.getTargetSum(),
            sum
        );
    }

    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }

    public List<UserPlanViewProjectionDto> findAllUserPlanViews() {
        return userPlanMapper.mapMany(planRepository.findAllUserPlanView());
    }

    public Optional<UserPlanViewProjectionDto> findActiveUserPlanById(Long userId) {
        return planRepository.finActivePlanByUser(userId)
            .map(userPlanMapper::map);
    }

    public record ValidationRequest(
        Long userId,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime start,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime end,
        Integer targetSum,
        Integer targetCount
    ) {
    }

    public record ValidationResponse(
        boolean valid,
        String message
    ) {
    }

    public record UserPlanDetailsDto(Long planId, String name, Long percent, Long targetSum, Long sum) {

        private static final DecimalFormat FORMATTER = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.ITALIAN));

        private String formatString(Long value) {
            return FORMATTER.format(value.longValue());
        }

        public String getTargetSumFormatted() {
            return formatString(targetSum);
        }

        public String getSumFormatted() {
            return formatString(sum);
        }
    }
}
