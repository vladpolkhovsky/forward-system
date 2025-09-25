import type {OrderShortDto} from "@/core/dto/OrderShortDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import type {OrderFullDto} from "@/core/dto/OrderFullDto.ts";

export type OrderCallback = (json: OrderShortDto) => void;
export type OrderListCallback = (json: PageableDto<OrderFullDto>) => void;
export type ReadyCallback = () => void;

export interface OrderSearch {
    techNumber: number,
    showClosed: boolean,
    status: string,
    page: number,
    size: number
}

export class OrderService {
    private constructor() {
    }

    public static fetchOrder(id: number, callback: OrderCallback) {
        fetch(`/api/order/${id}`, { method: 'GET'})
            .then(value => value.json())
            .then(value => callback(value as OrderShortDto));
    }

    public static searchOrders(criteria: OrderSearch, callback: OrderListCallback) {
        let urlSearchParams = new URLSearchParams(OrderService.removeNullProperties(criteria) as Record<string, string>);
        fetch(`/api/order/search?${urlSearchParams.toString()}`, { method: 'GET', })
            .then(value => value.json())
            .then(json => callback(json as PageableDto<OrderFullDto>))
    }

    public static deleteExpertFromOrder(id: number, callback: ReadyCallback) {
        fetch(`/api/order/remove-expert/${id}`, { method: "POST" })
            .then(_ => callback());
    }

    private static removeNullProperties<T extends object>(obj: T): Partial<T> {
        return Object.entries(obj).reduce((acc, [key, value]) => {
            if (value !== null) {
                acc[key as keyof T] = value;
            }
            return acc;
        }, {} as Partial<T>);
    }
}