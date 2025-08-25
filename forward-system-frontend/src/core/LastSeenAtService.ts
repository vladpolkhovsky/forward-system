export class LastSeenAtService {

    private static intervalId: number;

    private static cache: Map<number, LastSeenAtDto> = new Map<number, LastSeenAtDto>();

    private constructor() {

    }

    private static update(): void {
        fetch("activity/ping", {method: "GET"})
            .then(() => console.log("Онлайн статус обновлён."))
            .then(() => {
                const myHeaders = new Headers();
                myHeaders.append("Content-Type", "application/json; charset=UTF-8");

                fetch("/activity/all/activity", {
                    method: "GET",
                    headers: myHeaders,
                    redirect: "follow"
                }).then((resp) => resp.json())
                    .then(json => {
                        for (const jsonElement of json as LastSeenAtDto[]) {
                            LastSeenAtService.cache.set(jsonElement.id, jsonElement)
                        }
                    })
            })
    }

    public static start(): void {
        LastSeenAtService.intervalId = setInterval(() => {
            LastSeenAtService.update();
        }, 30000);

        LastSeenAtService.update();
    }

    public static stop(): void {
        clearInterval(LastSeenAtService.intervalId);
    }

    public static getUserLastSeenAt(userId: number): LastSeenAtDto {
        if (userId != null) {
            return LastSeenAtService.cache.get(userId);
        }
        return {
            online: false,
            shouldBeVisible: false,
            lastOnlineAt: "Никогда",
            id: null
        }
    }
}

export interface LastSeenAtDto {
    id: number,
    online: boolean,
    shouldBeVisible: boolean,
    lastOnlineAt: string;
}