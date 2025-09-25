export interface ReviewCreateRequestDto {
    orderId?: number;
    fileId?: number;
    chatId?: number;
    requestText?: string;
}