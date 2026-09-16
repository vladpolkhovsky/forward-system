import type {UserDto} from "@/core/dto/UserDto.ts";
import type {AuthorityType} from "@/core/type/AuthorityType.ts";
import type {ManagerSubDto} from "@/core/dto/ManagerSubDto.ts";

export type UserDataCallback = (json: UserDto) => void;
export type UserDataManyCallback = (json: UserDto[]) => void;
export type ManagerSubCallback = (json: ManagerSubDto) => void;

export class UserService {
    private constructor() {

    }

    public static fetchUserData(fetchAutomatically: boolean = true, userId: number = null, callback: UserDataCallback) {
        const fetchUrl = fetchAutomatically ? '/api/user/info' : `/api/user/info/${userId}`;
        fetch(fetchUrl, {method: "GET"})
            .then(value => value.json())
            .then(value => value as UserDto)
            .then(value => callback(value))
    }

    public static fetchAllUsersWithAuthority(authority: AuthorityType, callback: UserDataManyCallback) {
        const fetchUrl = `/api/user/${authority}`;
        fetch(fetchUrl, {method: "GET"})
            .then(value => value.json())
            .then(value => value as UserDto[])
            .then(value => callback(value))
    }

    public static fetchUserSub(callback: ManagerSubCallback) {
        const fetchUrl = `/api/manager/sub`;
        fetch(fetchUrl, {method: "GET"})
            .then(value => value.json())
            .then(value => value as ManagerSubDto)
            .then(value => callback(value))
    }

    public static updateUserSub(subUserId: number, callback: ManagerSubCallback) {
        const fetchUrl = `/api/manager/sub/${subUserId}`;
        fetch(fetchUrl, {method: "POST"})
            .then(value => value.json())
            .then(value => value as ManagerSubDto)
            .then(value => callback(value))
    }
}