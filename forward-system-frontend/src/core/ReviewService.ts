import type {V3SearchOrderReviewDto} from "@/core/dto/V3SearchOrderReviewDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import type {ReviewCreateRequestDto} from "@/core/dto/ReviewCreateRequestDto.ts";

export type LoadCallback = (json: PageableDto<V3SearchOrderReviewDto>) => void;
export type ReadyCallback = () => void;

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

    public static createReviewRequest(request: ReviewCreateRequestDto, callback: ReadyCallback) {
        fetch(`/api/review/create`, {
            method: 'POST',
            body: JSON.stringify(request),
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            }
        }).then(value => value.json())
            .then(value => {
                callback()
                if (value['code'] != 200) {
                    throw new Error(value['message']);
                }
            })
            .catch(err => alert(err['message']))
    }
}