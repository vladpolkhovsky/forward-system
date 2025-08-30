import type {UserDto} from "@/core/dto/UserDto.ts";

export interface DistributionItemLogDto {
    id: number,
    userId: number,
    user: UserDto,
    statusType: string,
    statusTypeRus: string,
    waitStart: string,
    waitUntil: string,
    createdAt: string
}