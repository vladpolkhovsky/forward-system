import type {ChatType} from "@/core/type/ChatType.ts";

export enum ChatTypeEnum {
    REQUEST_ORDER_CHAT = "REQUEST_ORDER_CHAT",
    ORDER_CHAT = "ORDER_CHAT",
    ADMIN_TALK_CHAT = "ADMIN_TALK_CHAT",
    OTHER_CHAT = "OTHER_CHAT",
    SPECIAL_CHAT = "SPECIAL_CHAT",
    FORWARD_ORDER_CHAT = "FORWARD_ORDER_CHAT",
    FORWARD_ORDER_ADMIN_CHAT = "FORWARD_ORDER_ADMIN_CHAT",
    PINNED = "PINNED"
}

export function chatTypeToRusName(chatType: ChatType): string {
    if (chatType == ChatTypeEnum.REQUEST_ORDER_CHAT) {
        return "Новые заказы"
    }
    if (chatType == ChatTypeEnum.ORDER_CHAT) {
        return "Чат заказа"
    }
    if (chatType == ChatTypeEnum.ADMIN_TALK_CHAT) {
        return "Администрация"
    }
    if (chatType == ChatTypeEnum.OTHER_CHAT) {
        return "Другие чаты"
    }
    if (chatType == ChatTypeEnum.SPECIAL_CHAT) {
        return "Специальные чаты"
    }
    if (chatType == ChatTypeEnum.FORWARD_ORDER_CHAT) {
        return "Чат напрямую с заказчиком"
    }
    if (chatType == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT) {
        return "Чат напрямую с заказчиком (администрация)"
    }
    if (chatType == ChatTypeEnum.PINNED) {
        return "Сохранённые"
    }
    return "Нет данных";
}