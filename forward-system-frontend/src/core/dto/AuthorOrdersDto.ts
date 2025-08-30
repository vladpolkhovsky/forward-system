import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import type {OrderPaymentStatusType} from "@/core/type/OrderPaymentStatusType.ts";
import type {AdditionalDateDto} from "@/core/dto/AdditionalDateDto.ts";

export interface AuthorOrdersDto {
    orderId: number,
    orderTechNumber: string,
    subject: string,
    originality: number,
    additionalDates: AdditionalDateDto[],
    deadline: string,
    intermediateDeadline: string,
    orderStatus: OrderStatusType,
    orderStatusRus: string,
    paymentStatus: OrderPaymentStatusType
}