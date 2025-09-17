export interface UserPlanViewDto {
    planId: number,
    planStart: string,
    planEnd: string,
    isPlanNotStartYet: boolean,
    isPlanActive: boolean,
    beforePlanBeginDays: number,
    beforePlanBeginHours: number,
    userId: number,
    username: string,
    targetSum: number,
    orderSum: number,
    orderSumPercent: number,
    targetCount: number,
    orderCountSum: number,
    orderCountSumPercent: number
}