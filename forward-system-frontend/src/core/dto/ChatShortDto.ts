import type {ChatType} from "@/core/type/ChatType.ts";

export interface ChatShortDto {
    id: number,
    name: string,
    displayName: string,
    lastMessageDate: string,
    realLastMessageDate: string,
    metadata: {
        chatId: number,
        chatType: ChatType,
        onlyOwnerCanType: boolean,
        authorCanSubmitFiles: boolean,
        managerId: number | null,
        authorId: number | null
    },
    tags: ChatTag[],
    orderId: number | null,
    orderTechNumber: string | null,
    orderStatus: string | null,
    orderStatusRus: string | null,
    isForwardOrder: boolean,
    isForwardOrderPaid: boolean,
    type: string,
    newMessageCount: number
}

export interface ChatTag {
    id: number,
    name: string,
    metadata: {
        type: string,
        isPrimaryTag: boolean,
        isVisible: boolean,
        isPersonalTag: boolean | null,
        userId: number | null,
        cssColorName: string | null
    }
}