import type {AuthorDisciplinesDto} from "@/core/dto/AuthorDisciplinesDto.ts";
import type {LastSeenAtDto} from "@/core/LastSeenAtService.ts";
import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";

export interface AuthorDto {
    id: number,
    username: string,
    disciplines: AuthorDisciplinesDto,
    activity: LastSeenAtDto,
    maxOrdersCount: number,
    activeOrders: AuthorOrdersDto[],
}