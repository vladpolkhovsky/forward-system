import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";

export interface CalendarGroupDto {
    id: number,
    name: string,
    participants: AuthorShortDto[]
}