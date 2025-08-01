import type {AuthorityType} from "@/core/type/AuthorityType.ts";

export interface UserDto {
    id: number,
    username: number,
    firstName: string,
    lastName: string,
    middleName: string,
    authorities: AuthorityType[]
}