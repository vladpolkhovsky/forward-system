import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";
import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import type {OrderPaymentStatusType} from "@/core/type/OrderPaymentStatusType.ts";

export interface OrderPaymentDto {
    id: number
    order: AuthorOrdersDto,
    user: AuthorShortDto,
    status: OrderPaymentStatusType,
    paymentValue: number | null,
    createdAt: string
}

export interface OrderPaymentSaveRequestDto {
    userId: number,
    orderId: number,
    status: OrderPaymentStatusType,
    paymentValue: number | null
}