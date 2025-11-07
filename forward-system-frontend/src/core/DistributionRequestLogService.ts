import type { PageableDto } from "@/core/dto/PageableDto.ts";
import type { DistributionRequestLogDto } from "@/core/dto/DistributionRequestLogDto.ts";

export type DistributionRequestLogCallback = (json: PageableDto<DistributionRequestLogDto>) => void;

export interface DistributionLogFilters {
    managerId?: number | null;
    authorId?: number | null;
    order?: string | null;
    page: number;
}

export class DistributionRequestLogService {
    public static getDistributionLogs(
        filters: DistributionLogFilters,
        callback: DistributionRequestLogCallback
    ): void {
        const url = this.buildUrl("/api/admin/order-request-log", filters);

        fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json; charset=UTF-8",
            },
        })
            .then((response) => response.json())
            .then((data) => callback(data as PageableDto<DistributionRequestLogDto>))
            .catch((error) => console.error("Fetch error:", error));
    }

    private static buildUrl(baseUrl: string, filters: DistributionLogFilters): string {
        const params = new URLSearchParams();

        // Проверяем на undefined и null, но разрешаем 0
        if (filters.managerId !== undefined && filters.managerId !== null) {
            params.append("managerId", filters.managerId.toString());
        }

        if (filters.authorId !== undefined && filters.authorId !== null) {
            params.append("authorId", filters.authorId.toString());
        }

        if (filters.order !== undefined && filters.order !== null) {
            params.append("order", filters.order);
        }

        params.append("page", filters.page.toString());

        const queryString = params.toString();
        return queryString ? `${baseUrl}?${queryString}` : baseUrl;
    }
}