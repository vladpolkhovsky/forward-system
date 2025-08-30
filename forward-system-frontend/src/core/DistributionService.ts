import type {DistributionRequest} from "@/core/dto/DistributionRequest.ts";
import type {DistributionLogDto} from "@/core/dto/DistributionLogDto.ts";

export type ReadyCallback = () => void;
export type DistributionLogsCallback = (logs: DistributionLogDto[]) => void;

export class DistributionService {
    private constructor() {
    }

    public static getDistributionLogs(orderId: number, callback: DistributionLogsCallback) {
        fetch(`/api/order/distribution?orderId=${orderId}`, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(value => callback(value as DistributionLogDto[]))
    }

    public static sendDistributionRequest(orderId: number, request: DistributionRequest, callback: ReadyCallback) {
        fetch(`/api/order/distribution?orderId=${orderId}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            },
            body: JSON.stringify(request)
        }).then(value => value.json())
            .then(_ => callback())
    }

    public static sendSkip(itemId: number, callback: ReadyCallback) {
        fetch(`/api/order/distribution/skip-item?itemId=${itemId}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(_ => callback())
    }

    public static sendSetNewCatcher(orderId: number, catcherId: number, callback: ReadyCallback) {
        fetch(`/api/order/distribution/set/catcher?orderId=${orderId}&catcherId=${catcherId}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(_ => callback())
    }

    public static sendStopDistribution(distributionId: number, callback: ReadyCallback) {
        fetch(`/api/order/distribution/stop?distributionId=${distributionId}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(value => {
                if (value['code'] != 200) {
                    throw new Error(value['message']);
                }
                callback()
            })
            .catch(err => alert(err['message']))
    }

    public static removeAuthorFromOrderParticipants(orderId: number, authorId: number, callback: ReadyCallback) {
        fetch(`/api/order/distribution/delete-author-participant?orderId=${orderId}&authorId=${authorId}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(value => {
                if (value['code'] != 200) {
                    throw new Error(value['message']);
                }
                callback()
            })
            .catch(err => alert(err['message']))
    }
}