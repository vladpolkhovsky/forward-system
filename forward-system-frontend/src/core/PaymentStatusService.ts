import type {OrderPaymentDto, OrderPaymentSaveRequestDto} from "@/core/dto/OrderPaymentDto.ts";

export type OrderPaymentsCallback = (payments: OrderPaymentDto[]) => void;
export type OrderPaymentCallback = (payments: OrderPaymentDto) => void;

export class PaymentStatusService {

    private constructor() {
    }

    public static save(payment: OrderPaymentSaveRequestDto, callback: OrderPaymentCallback) {
        this.saveMany([payment], (values) => {
            callback(values[0])
        })
    }

    public static saveMany(payments: OrderPaymentSaveRequestDto[], callback: OrderPaymentsCallback) {
        const headers = new Headers();
        headers.append("Content-Type", "application/json; charset=UTF-8");

        fetch("/api/order/payment/create/many", {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payments),
        }).then(value => value.json())
            .then(value => value as OrderPaymentDto[])
            .then(value => callback(value))
    }

    public static fetchPaymentStatus(callback: OrderPaymentsCallback): void {
        fetch(`/api/order/payment`, {method: "GET"})
            .then(value => value.json())
            .then(value => value as OrderPaymentDto[])
            .then((value: OrderPaymentDto[]) => {
                callback(value);
            })
    }

    public static fetchPaymentStatusByUserId(userId: number, callback: OrderPaymentsCallback): void {
        fetch(`/api/order/payment/${userId}`, {method: "GET"})
            .then(value => value.json())
            .then(value => value as OrderPaymentDto[])
            .then((value: OrderPaymentDto[]) => {
                callback(value);
            })
    }
}