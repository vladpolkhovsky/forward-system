import type {OrderPaymentStatusType} from "@/core/type/OrderPaymentStatusType.ts";

export enum OrderPaymentStatusEnum {
    FULL_PAYMENT = "FULL_PAYMENT",
    PARTITIONAL_PAYMENT = "PARTITIONAL_PAYMENT",
    NO_PAYMENT = "NO_PAYMENT"
}

export function paymentStatusToRusName(payment: OrderPaymentStatusType): string {
    if (payment == OrderPaymentStatusEnum.FULL_PAYMENT) {
        return "Оплачен"
    }
    if (payment == OrderPaymentStatusEnum.PARTITIONAL_PAYMENT) {
        return "Частично оплачен"
    }
    if (payment == OrderPaymentStatusEnum.NO_PAYMENT) {
        return "Не оплачен"
    }
    return "Нет данных";
}