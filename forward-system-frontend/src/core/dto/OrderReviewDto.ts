import type {AttachmentDto} from "@/core/dto/AttachmentDto.ts";

export interface OrderReviewDto {
    id: number,
    orderId: number,
    orderTechNumber: string,
    isApproved: boolean,
    requestText: string,
    resultText: string,
    resultMark: string,
    resultDate: string,
    createdAt: string,
    requestFile: AttachmentDto,
    resultFile: AttachmentDto
}