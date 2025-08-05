import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";

export type AuthorCallback = (authors: AuthorShortDto[]) => void;

export class AuthorService {
    private constructor() {

    }

    public static fetchAuthors(callback: AuthorCallback): void {
        fetch("/api/author/get-authors", {method: "GET"})
            .then(value => value.json())
            .then(value => value as AuthorShortDto[])
            .then((value: AuthorShortDto[]) => {
                callback(value);
            })
    }
}