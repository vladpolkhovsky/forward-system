import type {ChatType} from "@/core/type/ChatType.ts";

export interface ChatTabDescription {
    tabName: string,
    queryParam: string,
    chatTypes: ChatType[]
}