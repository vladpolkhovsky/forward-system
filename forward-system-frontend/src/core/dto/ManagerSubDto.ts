import type {UserDto} from "@/core/dto/UserDto.ts";

export interface ManagerSubDto {
    hasSubManager: boolean,
    subManager: UserDto
}