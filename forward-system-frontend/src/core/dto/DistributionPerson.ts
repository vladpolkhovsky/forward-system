import type {AuthorDto} from "@/core/dto/AuthorDto.ts";

export interface DistributionPerson {
    userId: number,
    author: AuthorDto,
    customFee?: number,
    order: number
}