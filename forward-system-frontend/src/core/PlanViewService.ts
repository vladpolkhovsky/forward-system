import type {UserPlanViewDto} from "@/core/dto/UserPlanViewDto.ts";

export type PlanCallback = (json: UserPlanViewDto) => void;
export type PlanListCallback = (json: UserPlanViewDto[]) => void;

export class PlanViewService {

    public static listPlans(planListCallback: PlanListCallback) {
        fetch(`/api/plan/user-plan-view`, {method: "GET"})
            .then(value => value.json())
            .then(value => value as UserPlanViewDto[])
            .then((value: UserPlanViewDto[]) => {
                planListCallback(value);
            });
    }

    public static getActivePlan(userId: number, planCallback: PlanCallback) {
        fetch(`/api/plan/user-plan-view/${userId}`, {method: "GET"})
            .then(value => value.json())
            .then(value => value as UserPlanViewDto)
            .then((value: UserPlanViewDto) => {
                planCallback(value);
            });
    }
}