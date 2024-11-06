package by.forward.forward_system.core.dto.rest;

import java.util.List;

public record AddParticipantRequestDto(List<Selected> selected,
                                       String role) {
    public record Selected(Long id, Integer fee) {

    }
}
