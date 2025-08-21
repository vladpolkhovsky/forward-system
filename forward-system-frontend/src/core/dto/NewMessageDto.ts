export interface NewMessageDto {
    id: number,
    chatId: number,
    chatName: string,
    fromUserId: number,
    fromUserUsername: string,
    message: string
}