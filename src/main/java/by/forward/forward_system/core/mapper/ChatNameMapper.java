package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.ChatMetadataEntity;
import by.forward.forward_system.core.utils.AuthUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatNameMapper {

    @Named("calculateDisplayName")
    default String calculateDisplayName(ChatEntity chat) {
        if (chat == null) {
            return null;
        }

        if (List.of(ChatType.FORWARD_ORDER_CHAT, ChatType.ORDER_CHAT).contains(chat.getChatType().getType())) {
            return "Заказ №" + chat.getOrder().getTechNumber();
        }
        if (ChatType.FORWARD_ORDER_ADMIN_CHAT.equals(chat.getChatType().getType())) {
            return "Заказ №" + chat.getOrder().getTechNumber() + " (Администрация)";
        }

        ChatMetadataEntity metadata = chat.getChatMetadata();
        if (metadata == null) {
            return chat.getChatName();
        }
        if (chat.getChatType().getType() == ChatType.REQUEST_ORDER_CHAT) {
            if (Objects.equals(AuthUtils.getCurrentUserId(), metadata.getAuthorId())) {
                return "Новые заказы от " + metadata.getManager().getUsername();
            }
            if (Objects.equals(AuthUtils.getCurrentUserId(), metadata.getManagerId())) {
                return "Новые заказы для " + metadata.getUser().getUsername();
            }
            return "Новые заказы";
        }

        if (chat.getChatType().getType() == ChatType.ADMIN_TALK_CHAT) {
            if (Objects.equals(AuthUtils.getCurrentUserId(), metadata.getAuthorId())) {
                return "Администрация";
            }
            if (chat.getChatMetadata().getUser() != null) {
                return "Чат с " + metadata.getUser().getUsername() + " (Администрация)";
            }
            return chat.getChatName();
        }

        return chat.getChatName();
    }
}
