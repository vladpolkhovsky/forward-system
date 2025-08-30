import type {UserDto} from "@/core/dto/UserDto.ts";
import type {ParticipantType} from "@/core/type/ParticipantType.ts";

export interface OrderParticipantDto {
    id: number,
    orderId: number,
    fee: number,
    user: UserDto,
    type: ParticipantType
    typeRusName: string
}