import type {ChatTabDescription} from "@/core/dto/ChatTabDescription.ts";
import {ChatTypeEnum} from "@/core/enum/ChatTypeEnum.ts";

export class ChatTabsService {
    private constructor() {
    }

    private static tabs: ChatTabDescription[] = [
        {
            tabName: "Все чаты",
            queryParam: "all",
            chatTypes: [
                ChatTypeEnum.REQUEST_ORDER_CHAT,
                ChatTypeEnum.ORDER_CHAT,
                ChatTypeEnum.ADMIN_TALK_CHAT,
                ChatTypeEnum.OTHER_CHAT,
                ChatTypeEnum.SPECIAL_CHAT,
                ChatTypeEnum.FORWARD_ORDER_CHAT,
                ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT
            ]
        },
        {
            tabName: "Новые заказы",
            queryParam: "new-orders",
            chatTypes: [ChatTypeEnum.REQUEST_ORDER_CHAT]
        },
        {
            tabName: "Заказы в работе",
            queryParam: "orders",
            chatTypes: [ChatTypeEnum.FORWARD_ORDER_CHAT, ChatTypeEnum.ORDER_CHAT]
        },
        {
            tabName: "Заказы в работе (Замена)",
            queryParam: "orders-sub",
            chatTypes: [ChatTypeEnum.ORDER_CHAT_SUB]
        },
        {
            tabName: "Прямые заказы",
            queryParam: "forward",
            chatTypes: [ChatTypeEnum.FORWARD_ORDER_CHAT, ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT]
        },
        {
            tabName: "Администрация",
            queryParam: "admin",
            chatTypes: [ChatTypeEnum.ADMIN_TALK_CHAT, ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT]
        },
        {
            tabName: "Спец. чаты",
            queryParam: "special",
            chatTypes: [ChatTypeEnum.SPECIAL_CHAT, ChatTypeEnum.OTHER_CHAT]
        }
        // {
        //     tabName: "Сохранённые",
        //     queryParam: "saved",
        //     chatTypes: [ChatTypeEnum.PINNED]
        // }
    ];

    public static getDefaultTabs() {
        return this.tabs;
    }
}