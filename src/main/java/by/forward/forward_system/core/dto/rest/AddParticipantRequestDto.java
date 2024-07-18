package by.forward.forward_system.core.dto.rest;

import java.util.List;

public record AddParticipantRequestDto(List<Long> ids,
                                       String role) {

}
