import type {AuthorDisciplinesDto} from "@/core/dto/AuthorDisciplinesDto.ts";
import type {LastSeenAtDto} from "@/core/LastSeenAtService.ts";

export interface AuthorDto {
    id: number,
    username: string,
    disciplines: AuthorDisciplinesDto,
    activity: LastSeenAtDto,
    maxOrdersCount: number,
    activeOrderIds: number[],
}