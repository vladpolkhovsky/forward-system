package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadIsChatMemberResponseDto {

    private List<IsChatMember> statuses;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IsChatMember {
        private Long id;
        private Boolean isMember;
    }
}
