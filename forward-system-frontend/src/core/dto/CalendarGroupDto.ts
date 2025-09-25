import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import type {OrderChatInformation} from "@/core/dto/OrderChatInformation.ts";

export interface CalendarGroupDto {
    id: number,
    name: string,
    participants: AuthorShortDto[],
    activeOrders: OrderChatInformation[],
}