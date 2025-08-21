package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ChatSearchCriteria {
    private String tagNameQuery;
    private String chatNameQuery;
    private List<ChatType> chatTypes;
}
