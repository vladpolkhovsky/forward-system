package by.forward.forward_system.core.dto.ui;

public record UserUiDto(Long id,
                        String username,
                        String password,
                        String fio,
                        String contact,
                        String contactTelegram,
                        String email,
                        String payment,
                        String other,
                        Boolean isAdmin,
                        Boolean isManager,
                        Boolean isAuthor,
                        Boolean isHr,
                        Boolean isOwner
                        ) {

}
