package by.forward.forward_system.core.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorUiDto {
    private Long id;
    private String username;
    private String password;
    private String fio;
    private String fioFull;
    private String firstname;
    private String lastname;
    private String surname;
    private String contact;
    private String contactTelegram;
    private String email;
    private String payment;
    private String other;
    private String excellentSubjects;
    private String goodSubjects;
    private String maybeSubjects;
    private Boolean isAdmin;
    private Boolean isManager;
    private Boolean isAuthor;
    private Boolean isHr;
    private Boolean isOwner;
    private Boolean isBanned;
    private String rolesRus;
}
