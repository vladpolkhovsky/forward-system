import type {AuthorDto} from "@/core/dto/AuthorDto.ts";

export type AuthorCallback = (authors: AuthorDto[]) => void;

export class AuthorService {
    private constructor() {

    }

    public static fetchAuthors(callback: AuthorCallback): void {
        fetch("/api/author/get-authors", {method: "GET"})
            .then(value => value.json())
            .then(value => value as AuthorDto[])
            .then((value: AuthorDto[]) => {
                callback(value);
            })
    }
}