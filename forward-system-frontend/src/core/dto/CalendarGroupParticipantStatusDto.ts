export interface CalendarGroupParticipantStatusDto {
    groupId: number,
    days: {
        [key: string]: number[]
    }
}