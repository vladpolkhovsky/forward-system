import type {JsonCallback} from "@/core/ChatService.ts";
import type {OrderChatInformation} from "@/core/dto/OrderChatInformation.ts";
import type {OrderReviewDto} from "@/core/dto/OrderReviewDto.ts";
import type {ForwardOrderChatInformation} from "@/core/dto/ForwardOrderChatInformation.ts";

export type ChatOrderCallback = (orderChatInfo: OrderChatInformation) => void;
export type OrderReviewsCallback = (orderReviews: OrderReviewDto[]) => void;
export type ForwardOrderCallback = (forwardOrderInfo: ForwardOrderChatInformation) => void;
export type ReadyCallback = () => void;

export class ChatSidebarService {
    private constructor() {

    }

    public static loadChatOrderInformation(orderId: number, chatOrderCallback: ChatOrderCallback) {
        ChatSidebarService.fetchData("GET", `/api/v3/chat/info/order/${orderId}`, null, (json) => {
            chatOrderCallback(json as OrderChatInformation);
        })
    }

    public static loadOrderReviews(orderId: number, orderReviewsCallback: OrderReviewsCallback) {
        ChatSidebarService.fetchData("GET", `/api/v3/chat/info/order/review/${orderId}`, null, (json) => {
            orderReviewsCallback(json as OrderReviewDto[]);
        })
    }

    public static loadForwardOrderChatInfo(orderId: number, forwardOrderCallback: ForwardOrderCallback) {
        ChatSidebarService.fetchData("GET", `/api/v3/chat/info/order/forward/${orderId}`, null, (json) => {
            forwardOrderCallback(json as ForwardOrderChatInformation);
        })
    }

    public static deleteForwardOrderUsers(forwardOrderId: number, readyCallback: ReadyCallback) {
        ChatSidebarService.fetchData("POST", `/api/v3/chat/info/order/forward/delete-all-telegram-chat-customers/${forwardOrderId}`, null, () => {
            readyCallback();
        });
    }

    public static changeForwardOrderPaidStatus(forwardOrderId: number, readyCallback: ReadyCallback) {
        ChatSidebarService.fetchData("POST", `/api/v3/chat/info/order/forward/status/paid/change/${forwardOrderId}`, null, () => {
            readyCallback();
        });
    }

    public static changeForwardOrderFileSubmissionStatus(forwardOrderId: number, readyCallback: ReadyCallback) {
        ChatSidebarService.fetchData("POST", `/api/v3/chat/info/order/forward/status/submit-files/change/${forwardOrderId}`, null, () => {
            readyCallback();
        });
    }

    private static fetchData(method: string, url: string, body: any, jsonCallback: JsonCallback): void {
        console.log("fetch", method, url, body);
        const headers = new Headers();
        headers.append("Content-Type", "application/json; charset=UTF-8");
        fetch(url, {
            method: method,
            headers: headers,
            body: body ? JSON.stringify(body) : undefined
        }).then(value => value.json()).then(value => {
            console.log(`fetched: ${method} : ${url}`, value)
            jsonCallback(value);
        })
    }


}