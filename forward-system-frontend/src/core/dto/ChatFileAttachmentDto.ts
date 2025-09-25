import type {AttachmentDto} from "@/core/dto/AttachmentDto.ts";
import type {UserDto} from "@/core/dto/UserDto.ts";

export interface ChatFileAttachmentDto {
    attachment: AttachmentDto;
    user: UserDto;
    createdAt: string
}