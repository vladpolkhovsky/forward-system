import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import type {OrderPaymentStatusType} from "@/core/type/OrderPaymentStatusType.ts";

export type AdditionalDate = {
    text: string,
    time: string
};

export interface AuthorOrdersDto {
    orderId: number,
    orderTechNumber: string,
    subject: string,
    originality: number,
    additionalDates: AdditionalDate[],
    deadline: string,
    intermediateDeadline: string,
    orderStatus: OrderStatusType,
    orderStatusRus: string
    paymentStatus: OrderPaymentStatusType
}