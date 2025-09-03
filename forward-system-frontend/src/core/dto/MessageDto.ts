import type {ChatOptionDto} from "@/core/dto/ChatOptionDto.ts";
import type {AttachmentDto} from "@/core/dto/AttachmentDto.ts";
import type {MessageType} from "@/core/type/MessageType.ts";
import type {ParticipantType} from "@/core/type/ParticipantType.ts";

export interface MessageDto {
    id: number,
    chatId: number,
    isSystemMessage: boolean,
    fromUserId: number,
    fromUserUsername: string,
    fromUserOrderParticipantType: ParticipantType,
    fromUserOrderParticipantTypeRusName: string,
    fromUserIsAdmin: boolean,
    fromUserIsDeleted: boolean,
    createdAt: string,
    realCreatedAt: string,
    isNewMessage: boolean,
    messageType: MessageType,
    messageReadedByUsernames: String[],
    text: string,
    options: ChatOptionDto[],
    attachments: AttachmentDto[]
}