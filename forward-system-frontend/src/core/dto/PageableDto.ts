export interface PageableDto<T> {
    content: T[],
    pageable: {
        pageNumber: number,
        pageSize: number,
    },
    last: boolean,
    first: boolean,
    totalElements: number,
    numberOfElements: number,
    totalPages: number,
    size: number,
    number: number,
    empty: number
}