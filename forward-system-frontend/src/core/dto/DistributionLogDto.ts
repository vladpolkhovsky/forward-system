import type {DistributionItemLogDto} from "@/core/dto/DistributionItemLogDto.ts";
import type {UserDto} from "@/core/dto/UserDto.ts";

export interface DistributionLogDto {
    id: number,
    orderId: number
    items: DistributionItemLogDto[],
    statusType: string,
    statusTypeRus: string,
    createdBy: UserDto,
    createdAt: string,
    startTimeAt: string,
}