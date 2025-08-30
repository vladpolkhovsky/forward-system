import type {OrderShortDto} from "@/core/dto/OrderShortDto.ts";

export type OrderCallback = (json: OrderShortDto) => void;

export class OrderService {
    private constructor() {
    }

    public static fetchOrder(id: number, callback: OrderCallback) {
        fetch(`/api/order/${id}`, { method: 'GET'})
            .then(value => value.json())
            .then(value => callback(value as OrderShortDto));
    }
}