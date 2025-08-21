export interface OrderChatInformation {
    id: number,
    name: string,
    techNumber: string,
    subject: string,
    status: string,
    statusRusName: string,
    disciplineId: number,
    disciplineName: string,
    deadline: string,
    intermediateDeadline: string,
    additionalDates: AdditionalDate[]
    authorUsername: string,
    managerUsername: string,
    verificationSystem: string,
    originality: number
}


export type AdditionalDate = {
    text: string,
    time: string
};