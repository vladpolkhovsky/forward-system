import type {AuthorityType} from "@/core/type/AuthorityType.ts";

export interface UserDto {
    id: number,
    username: string,
    firstName: string,
    lastName: string,
    middleName: string,
    authorities: AuthorityType[]
}