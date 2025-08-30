import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";

export type ParticipantType = keyof typeof ParticipantTypeEnum;

export function getParticipantTypeRusName(type: ParticipantType): String {
    if (type == ParticipantTypeEnum.HOST) {
        return "Менеджер Хост";
    }
    if (type == ParticipantTypeEnum.CATCHER) {
        return "Менеджер Кетчер";
    }
    if (type == ParticipantTypeEnum.AUTHOR) {
        return "Автор на рассмотрении";
    }
    if (type == ParticipantTypeEnum.MAIN_AUTHOR) {
        return "Взявший в работу автор";
    }
    if (type == ParticipantTypeEnum.DECLINE_AUTHOR) {
        return "Отказавщийся автор";
    }
}