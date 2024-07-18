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
    private String contact;
    private String contactTelegram;
    private String email;
    private String payment;
    private String other;
    private String subject;
    private String quality;
    private Boolean isAdmin;
    private Boolean isManager;
    private Boolean isAuthor;
    private Boolean isHr;
    private Boolean isOwner;
}
