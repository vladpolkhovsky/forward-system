import type {DistributionPerson} from "@/core/dto/DistributionPerson.ts";

export interface DistributionRequest {
    text: string,
    isQueueDistribution: boolean,
    queueDistributionWaitMinutes: number,
    persons: DistributionPerson[]
}