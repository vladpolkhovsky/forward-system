package by.forward.forward_system.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatNames {
    public static final String ORDER_REQUEST_CHAT = "Заказ №%s. Обсуждение с автором %s";
    public static final String ORDER_CHAT = "Заказ №%s. Основной чат заказа.";

    public static final String FORWARD_ORDER_CHAT = "Прямой заказ №%s";
    public static final String ADMIN_FORWARD_ORDER_CHAT = "Прямой заказ №%s. Администрация";

    public static final String NEW_ORDER_CHAT_NAME = "НОВЫЕ ЗАКАЗЫ для %s от %s";
    public static final String ADMINISTRATION_CHAT_NAME = "Чат с Администрацией %s";

    public static final Long NEWS_CHAT_ID = 0L;
    public static final Long ERRORS_CHAT_ID = -1L;
    public static final Long EXPERT_USER_ID = -10L;
    public static final Long ADMIN_USER_ID = -20L;
    public static final Long ALWAYS_SHOW_NAME_ID_LIMIT = 0L;

    public static final String JOIN_FORWARD_ORDER_HTML = "<kbd>/join %s</kbd>";
}
