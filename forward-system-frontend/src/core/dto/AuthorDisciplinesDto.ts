import type {DisciplineDto} from "@/core/dto/DisciplineDto.ts";
import type {LastSeenAtDto} from "@/core/LastSeenAtService.ts";

export interface AuthorDisciplinesDto {
    userId: number,
    excellent: DisciplineDto[],
    good: DisciplineDto[],
    maybe: DisciplineDto[],
    activity: LastSeenAtDto,
    activeOrderIds: number[],
    maxOrdersCount: number
}