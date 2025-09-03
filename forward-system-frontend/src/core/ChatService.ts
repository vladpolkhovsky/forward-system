import type {ChatType} from "@/core/type/ChatType.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";
import type {MessageDto} from "@/core/dto/MessageDto.ts";
import type {NewMessageDto} from "@/core/dto/NewMessageDto.ts";
import type {NewMessageCountDto} from "@/core/dto/NewMessageCountDto.ts";

export type JsonCallback = (json: object) => void;
export type SearchChatCallback = (page: PageableDto<ChatShortDto>) => void;
export type ChatCallback = (chat: ChatShortDto) => void;
export type MessagePageCallback = (page: PageableDto<MessageDto>) => void;
export type MessageCallback = (message: MessageDto) => void;
export type NewMessageCallback = (message: NewMessageDto, chat: ChatShortDto) => void;
export type ReadyCallback = () => void;
export type NewMessageCountCallback = (newMessageDto: NewMessageCountDto) => void;

export class ChatService {

    private static readonly FETCH_DALEY = 2400;

    private readonly userId: number;

    private updateIntervalId: number;
    private serverTime: string;
    private chatId: number;
    private tabs: ChatType[];

    private fetchedChats: Map<Number, ChatShortDto> = new Map();
    private static fetchedMessageIds: Set<Number> = new Set();

    public constructor(userId: number, chatId: number, tabs: ChatType[]) {
        this.userId = userId;
        this.chatId = chatId;
        this.tabs = tabs;
    }

    public updateChatId(chatId: number) {
        this.chatId = chatId;
    }

    public start(readyCallback: ReadyCallback, newMessageCallback: NewMessageCallback) {
        console.log("Старт сервиса чата");
        ChatService.fetchData("GET", "/api/new-chat/server-time", null, json => {
            this.serverTime = (json as { time: string }).time;
            readyCallback();
            this.updateIntervalId = setInterval(() => {
                console.log("Обновляем информацию о сообщениях", {
                    userId: this.userId,
                    chatId: this.chatId,
                    serverTime: this.serverTime
                })

                const url = (this.chatId ? `/api/new-chat/new-messages-ids-by-http/${this.userId}/${this.chatId}`
                    : `/api/new-chat/new-messages-ids-by-http/${this.userId}`) + `?after=${this.serverTime}`;

                ChatService.fetchData("GET", url, null, json => {
                    const newMessages = json as NewMessageDto[];
                    const reallyNewMessages = newMessages.filter(msg => {
                        const isNew = !ChatService.fetchedMessageIds.has(msg.id);
                        ChatService.fetchedMessageIds.add(msg.id);
                        return isNew;
                    });

                    const chatIdToMessages: Map<number, NewMessageDto[]> = new Map<number, NewMessageDto[]>();
                    reallyNewMessages.forEach(msg => {
                        let chats = chatIdToMessages.get(msg.chatId) ?? [];
                        chats.push(msg);
                        chatIdToMessages.set(msg.chatId, chats);
                    });

                    console.log("new messages ", reallyNewMessages);

                    chatIdToMessages.forEach((messages, chatId) => {
                        messages.sort((a, b) => a.id - b.id).forEach(msg => {
                            this.fetchChat(chatId, chat => {
                                console.log("publish new message event for chat and message", chat, msg);
                                newMessageCallback(msg, chat);
                            });
                        });
                    });
                });
            }, ChatService.FETCH_DALEY);
        });
    }

    public search(query: string = null, page: number = 0, callback: SearchChatCallback) {
        let request = {
            chatNameQuery: query,
            tagNameQuery: query,
            chatTypes: this.tabs
        };
        ChatService.fetchData("POST", `/api/v3/chat/search?page=${page}`, request, json => {
            console.log("Search ", json)
            const asChatsPage = json as PageableDto<ChatShortDto>;

            for (let chat of asChatsPage.content) {
                this.fetchedChats.set(chat.id, chat);
            }

            callback(json as PageableDto<ChatShortDto>)
        })
    }

    private firstFetchMessageChatTime: Map<number, string> = new Map<number, string>();

    public loadChatMessages(chatId: number, page: number, messagePageCallback: MessagePageCallback): void {
        ChatService.fetchData("GET", "/api/new-chat/server-time", null, json => {
            if (page == 0) {
                this.firstFetchMessageChatTime.set(chatId, (json as { time: string }).time);
            }
            const time = this.firstFetchMessageChatTime.get(chatId);
            ChatService.fetchData("GET", `/api/v3/chat/message?chatId=${chatId}&after=${time}&page=${page}`, null, json => {
                messagePageCallback(json as PageableDto<MessageDto>);
            })
        });
    }

    public sendMessageViewed(chatId: number) {
        ChatService.fetchData("POST", `/api/messenger/message-viewed/${chatId}`, null, () => {
        });
    }

    public fetchChat(id: number, chatCallback: ChatCallback, refresh: boolean = false) {
        const chat = this.fetchedChats.get(id);
        if (chat) {
            if (refresh) {
                ChatService.fetchChatById(id, refreshedChat => {
                    this.fetchedChats.set(id, refreshedChat);
                    chatCallback(refreshedChat);
                });
            }
            return chatCallback(chat);
        }
        ChatService.fetchChatById(id, (fetchedChat) => {
            this.fetchedChats.set(id, fetchedChat);
            chatCallback(fetchedChat);
        });
    }

    public static fetchMessageById(messageId: number, messageCallback: MessageCallback) {
        console.log("force fetch message id = ", messageId)
        ChatService.fetchData("GET", `api/v3/chat/message/id/${messageId}`, null, (json) => {
            messageCallback(json as MessageDto);
        })
    }

    public static fetchNewMessageCount(newMessageCountCallback: NewMessageCountCallback) {
        console.log("update new message chat tab count")
        ChatService.fetchData("GET", `api/v3/chat/count`, null, (json) => {
            newMessageCountCallback(json as NewMessageCountDto);
        })
    }

    private static fetchChatById(chatId: number, chatCallback: ChatCallback): void {
        console.log("force fetch chat id = ", chatId)
        ChatService.fetchData("GET", `api/v3/chat/id/${chatId}`, null, (json) => {
            chatCallback(json as ChatShortDto);
        })
    }

    public close() {
        clearInterval(this.updateIntervalId);
    }

    public static sendMessageToChat(userId: number, chatId: number, text: string, attachmentsIds: number[], readyCallback: ReadyCallback): void {
        const body = {
            chatId: chatId,
            userId: userId,
            text: text,
            attachmentsIds: attachmentsIds
        };
        ChatService.fetchData("POST", "/api/v3/chat/send", body, (json) => {
            console.log('message send', json)
            readyCallback();
        })
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