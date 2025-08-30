import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import type {AdditionalDateDto} from "@/core/dto/AdditionalDateDto.ts";
import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";

export interface OrderShortDto {
    id: number,
    techNumber: number,
    subject: string,
    disciplineName: string,
    disciplineId: number,
    originality: number,
    deadline: string,
    intermediateDeadline: string,
    additionalDates: AdditionalDateDto[],
    participants: OrderParticipantDto[];
    orderStatus: OrderStatusType,
    orderStatusRus: string,
    orderCost: number,
    orderAuthorCost: number
}