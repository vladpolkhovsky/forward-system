export class QueryParamService {

    private static urlSearchParams: URLSearchParams = null;

    private static getUrlSearchParams() {
        QueryParamService.urlSearchParams = QueryParamService.urlSearchParams ?? new URLSearchParams(window.location.search);
        return QueryParamService.urlSearchParams;
    }

    private constructor() {

    }

    public static setParam(paramName: string, value: string): string {
        const currentUrl = new URL(window.location.href);
        const params = QueryParamService.getUrlSearchParams();

        params.set(paramName, value);

        const newUrl = `${currentUrl.origin}${currentUrl.pathname}?${params.toString()}`;
        window.history.replaceState({}, '', newUrl);

        return value;
    }

    public static removeParam(paramName: string) {
        if (QueryParamService.getUrlSearchParams().has(paramName)) {
            QueryParamService.getUrlSearchParams().delete(paramName);
        }
    }

    public static getParam(paramName: string): string {
        if (QueryParamService.getUrlSearchParams().has(paramName)) {
            return QueryParamService.getUrlSearchParams().get(paramName);
        }
        return null;
    }
}