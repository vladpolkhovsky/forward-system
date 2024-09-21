package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.dto.ui.AuthorUiDto;
import by.forward.forward_system.core.dto.ui.UserSelectionUiDto;
import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.ui.AuthorUiService;
import by.forward.forward_system.core.services.ui.UserUiService;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Controller
@AllArgsConstructor
public class CreateChatController {

    private final UserUiService userUiService;

    private final ChatService chatService;

    private final AuthorUiService authorUiService;

    private final static String namePattern = "%s (%s)";

    @GetMapping(value = "/create-chat")
    public String createChat(Model model) {
        userUiService.checkAccessOwner();

        Function<UserUiDto, UserSelectionUiDto> userMap = t -> new UserSelectionUiDto(t.getId(), namePattern.formatted(t.getFio(), t.getRolesRus()), t.getUsername(), false);
        Function<AuthorUiDto, UserSelectionUiDto> authorMap = t -> new UserSelectionUiDto(t.getId(), namePattern.formatted(t.getFio(), t.getRolesRus()), t.getUsername(), false);

        model.addAttribute("menuName", "Создать чат");
        model.addAttribute("userShort", userUiService.getCurrentUser());

        model.addAttribute("chatId", null);
        model.addAttribute("chatName", null);
        model.addAttribute("users", userUiService.getAllUsers().stream().map(userMap).sorted(Comparator.comparing(UserSelectionUiDto::getUsername)).toList());
        model.addAttribute("authors", authorUiService.getAllAuthors().stream().map(authorMap).sorted(Comparator.comparing(UserSelectionUiDto::getUsername)).toList());

        return "main/create-chat";
    }

    @GetMapping(value = "/update-chat")
    public String updateChat(Model model) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Изменить чат");
        model.addAttribute("userShort", userUiService.getCurrentUser());

        List<ChatDto> allChats = chatService.getAllChats();

        List<ChatDto> createdChats = allChats.stream().sorted(Comparator.comparing(ChatDto::getChatName)).filter(t -> t.getChatType().equals(ChatType.SPECIAL_CHAT.getName())).toList();
        List<ChatDto> systemChats = allChats.stream().sorted(Comparator.comparing(ChatDto::getChatName)).filter(t -> !t.getChatType().equals(ChatType.SPECIAL_CHAT.getName())).toList();

        model.addAttribute("createdChats", createdChats);
        model.addAttribute("systemChats", systemChats);

        return "main/update-chat-selector";
    }

    @GetMapping(value = "/update-chat/{chatId}")
    public String updateChat(Model model, @PathVariable Long chatId) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Создать пользователя");
        model.addAttribute("userShort", userUiService.getCurrentUser());

        Set<Long> chatMembers = chatService.getChatMembers(chatId);
        String chatName = chatService.getChat(chatId).getChatName();

        Function<UserUiDto, UserSelectionUiDto> userMap = t -> new UserSelectionUiDto(t.getId(), namePattern.formatted(t.getFio(), t.getRolesRus()), t.getUsername(), chatMembers.contains(t.getId()));
        Function<AuthorUiDto, UserSelectionUiDto> authorMap = t -> new UserSelectionUiDto(t.getId(), namePattern.formatted(t.getFio(), t.getRolesRus()), t.getUsername(), chatMembers.contains(t.getId()));

        model.addAttribute("chatId", chatId);
        model.addAttribute("chatName", chatName);
        model.addAttribute("users", userUiService.getAllUsers().stream().map(userMap).sorted(Comparator.comparing(UserSelectionUiDto::getUsername)).toList());
        model.addAttribute("authors", authorUiService.getAllAuthors().stream().map(authorMap).sorted(Comparator.comparing(UserSelectionUiDto::getUsername)).toList());

        return "main/create-chat";
    }

    @GetMapping(value = "/delete-chat/{chatId}")
    public RedirectView deleteChat(@PathVariable Long chatId) {
        userUiService.checkAccessOwner();

        chatService.deleteChat(chatId);

        return new RedirectView("/update-chat");
    }

    @PostMapping(value = "/create-chat")
    public RedirectView saveChat(@RequestBody MultiValueMap<String, String> body) {
        userUiService.checkAccessOwner();

        String chatId = body.getFirst("chatId");
        String chatName = body.getFirst("chatName");
        List<Long> chatMembers = body.get("users").stream().map(Long::valueOf).toList();

        if (StringUtils.isBlank(chatId)) {
            chatService.createChat(chatName, chatMembers);
        } else {
            chatService.updateChat(Long.valueOf(chatId), chatName, chatMembers);
        }

        return new RedirectView("/main");
    }

}
