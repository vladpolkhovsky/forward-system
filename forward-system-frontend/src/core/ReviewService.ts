import type {V3SearchOrderReviewDto} from "@/core/dto/V3SearchOrderReviewDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";

export type LoadCallback = (json: PageableDto<V3SearchOrderReviewDto>) => void;

export class ReviewService {
    private constructor() {
    }

    public static searchReviewsByTechNumberLike(techNumber: string, page: number = 0, callback: LoadCallback) {
        fetch(`/api/review/search?techNumber=${techNumber}&page=${page}`, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(page => callback(page));
    }
}