import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";

export type AuthorOrderCallback = (authors: AuthorOrdersDto[]) => void;

export class AuthorOrderService {
    private constructor() {

    }

    public static fetchAuthorOrders(callback: AuthorOrderCallback): void {
        fetch("/api/author/get-author-orders", {method: "GET"})
            .then(value => value.json())
            .then(value => value as AuthorOrdersDto[])
            .then((value: AuthorOrdersDto[]) => {
                callback(value);
            })
    }

    public static fetchAuthorOrdersById(authorId: number, callback: AuthorOrderCallback): void {
        fetch(`/api/author/get-author-orders/${authorId}`, {method: "GET"})
            .then(value => value.json())
            .then(value => value as AuthorOrdersDto[])
            .then((value: AuthorOrdersDto[]) => {
                callback(value);
            })
    }
}