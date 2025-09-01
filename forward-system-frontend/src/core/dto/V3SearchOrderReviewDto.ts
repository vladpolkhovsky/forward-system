import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import type {OrderReviewDto} from "@/core/dto/OrderReviewDto.ts";

export interface V3SearchOrderReviewDto {
    orderId: number
    orderTechNumber: string
    status: OrderStatusType
    statusRusName: string
    reviews: OrderReviewDto[]
}