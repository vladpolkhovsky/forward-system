import type {AuthorityType} from "@/core/type/AuthorityType.ts";

export enum AuthorityEnum {
    OWNER = "OWNER",
    ADMIN = "ADMIN",
    MANAGER = "MANAGER",
    HR = "HR",
    AUTHOR = "AUTHOR",
    BANNED = "BANNED",
    DELETED = "DELETED",
    ACCOUNTANT = "ACCOUNTANT"
}

export function authorityToRusName(authority: AuthorityType): string {
    if (authority == AuthorityEnum.OWNER) {
        return "Владелец";
    }
    if (authority == AuthorityEnum.ADMIN) {
        return "Администратор";
    }
    if (authority == AuthorityEnum.MANAGER) {
        return "Менеджер";
    }
    if (authority == AuthorityEnum.HR) {
        return "HR";
    }
    if (authority == AuthorityEnum.AUTHOR) {
        return "Автор";
    }
    if (authority == AuthorityEnum.BANNED) {
        return "Заблокирован";
    }
    if (authority == AuthorityEnum.DELETED) {
        return "Удалён";
    }
    if (authority == AuthorityEnum.ACCOUNTANT) {
        return "Бухгалтер";
    }
    return authority;
}

export function hasAuthorityManager(authorities: AuthorityType[]): boolean {
    return authorities
        ?.map(value => AuthorityEnum[value])
        ?.filter(value => AuthorityEnum.ADMIN == value || AuthorityEnum.MANAGER == value || AuthorityEnum.OWNER == value)
        ?.length > 0;
}

export function hasAuthority(authorities: AuthorityType[], targetAuthority: AuthorityEnum): boolean {
    return authorities
        ?.map(value => AuthorityEnum[value])
        ?.filter(value => targetAuthority == value)?.length > 0;
}