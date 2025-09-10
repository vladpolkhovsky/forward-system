import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import type {AdditionalDateDto} from "@/core/dto/AdditionalDateDto.ts";
import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";

export interface ManagerOrdersDto {
    orderId: number,
    orderTechNumber: string,
    subject: string,
    originality: number,
    additionalDates: AdditionalDateDto[],
    deadline: string,
    intermediateDeadline: string,
    orderStatus: OrderStatusType,
    orderStatusRus: string,
    participants: OrderParticipantDto[]
}