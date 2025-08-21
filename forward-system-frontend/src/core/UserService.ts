import type {UserDto} from "@/core/dto/UserDto.ts";

export type UserDataCallback = (json: UserDto) => void;

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
}