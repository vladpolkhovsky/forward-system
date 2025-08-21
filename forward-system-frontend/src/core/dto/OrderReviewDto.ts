import type {AttachmentDto} from "@/core/dto/AttachmentDto.ts";

export interface OrderReviewDto {
    id: number,
    orderId: number,
    isApproved: boolean,
    requestText: string,
    resultText: string,
    resultMark: string,
    requestDate: string,
    resultDate: string,
    createdAt: string,
    requestFile: AttachmentDto,
    resultFile: AttachmentDto
}