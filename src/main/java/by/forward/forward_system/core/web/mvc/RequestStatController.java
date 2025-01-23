package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.repository.fast.FastReadOrderRequestStatRepository;
import by.forward.forward_system.core.jpa.repository.fast.FastUserReadRepository;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class RequestStatController {

    private final UserUiService userUiService;

    private static final DateTimeFormatter htmlDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final FastUserReadRepository fastUserReadRepository;
    private final FastReadOrderRequestStatRepository fastOrderRequestReadRepository;

    @GetMapping("/req-stat-redirect")
    public RedirectView redirect(@RequestParam(value = "type", required = false) String type) {
        LocalDate end = LocalDate.now();

        if (type == null) {
            return new RedirectView("/req-stat-redirect?type=last7");
        }

        switch (type) {
            case "last7" -> {
                LocalDate start = end.minusDays(7);
                return new RedirectView(makeRedirectUrl(start, end));
            }
            case "last14" -> {
                LocalDate start = end.minusDays(14);
                return new RedirectView(makeRedirectUrl(start, end));
            }
            case "last30" -> {
                LocalDate start = end.minusDays(30);
                return new RedirectView(makeRedirectUrl(start, end));
            }
            case "last60" -> {
                LocalDate start = end.minusDays(60);
                return new RedirectView(makeRedirectUrl(start, end));
            }
            case "last90" -> {
                LocalDate start = end.minusDays(90);
                return new RedirectView(makeRedirectUrl(start, end));
            }
            default -> {
                return new RedirectView("/req-stat-redirect?type=last7");
            }
        }
    }

    private String makeRedirectUrl(LocalDate start, LocalDate end) {
        return "/req-stat?startDate=" + htmlDateTimeFormat.format(start) + "&endDate=" + htmlDateTimeFormat.format(end);
    }

    @GetMapping("/req-stat")
    public String mainMenu(@RequestParam("startDate") String startDate,
                           @RequestParam("endDate") String endDate,
                           Model model) {

        userUiService.checkAccessAdmin();

        LocalDate currentDate = LocalDate.now();
        LocalDate start = LocalDate.from(htmlDateTimeFormat.parse(startDate));
        LocalDate end = LocalDate.from(htmlDateTimeFormat.parse(endDate));

        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        List<FastUserReadRepository.UserDto> users = fastUserReadRepository.readAllActiveUsers();
        users.sort(Comparator.comparing(a -> a.getUsername().toLowerCase()));

        List<FastReadOrderRequestStatRepository.StatDto> statDto = fastOrderRequestReadRepository.readAllStatInInterval(start, end);

        Set<Long> statAId = statDto.stream().map(FastReadOrderRequestStatRepository.StatDto::getAuthorId).collect(Collectors.toSet());
        Set<Long> statMId = statDto.stream().map(FastReadOrderRequestStatRepository.StatDto::getManagerId).collect(Collectors.toSet());
        DataMap dataMap = new DataMap(statAId, statMId, statDto);

        List<Long> aIds = users.stream().filter(t -> t.is(Authority.AUTHOR)).map(FastUserReadRepository.UserDto::getId).collect(Collectors.toList());
        List<Long> mIds = users.stream().filter(t -> t.is(Authority.MANAGER)).map(FastUserReadRepository.UserDto::getId).collect(Collectors.toList());
        Map<Long, String> uMap = users.stream().collect(Collectors.toMap(FastUserReadRepository.UserDto::getId, FastUserReadRepository.UserDto::getUsername));

        model.addAttribute("userShort", userUiService.getCurrentUser());

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("cDate", currentDate);
        model.addAttribute("daysCalculator", ChronoUnit.DAYS);
        model.addAttribute("uMap", uMap);
        model.addAttribute("aIds", aIds);
        model.addAttribute("mIds", mIds);
        model.addAttribute("dataMap", dataMap);

        return "main/request-stat";
    }

    public static class DataMap {

        private final Map<IdPair, List<FastReadOrderRequestStatRepository.StatDto>> dataMap = new HashMap<>();

        private final Set<Long> aIds;
        private final Set<Long> mIds;

        private final int totalSum;

        public DataMap(Set<Long> aIds, Set<Long> mIds, List<FastReadOrderRequestStatRepository.StatDto> data) {
            for (Long aId : aIds) {
                for (Long mId : mIds) {
                    dataMap.put(new IdPair(aId, mId), new ArrayList<>());
                }
            }
            for (FastReadOrderRequestStatRepository.StatDto datum : data) {
                dataMap.get(new IdPair(datum.getAuthorId(), datum.getManagerId())).add(datum);
            }
            this.aIds = aIds;
            this.mIds = mIds;
            this.totalSum = data.size();
        }

        public int getCount(Long aId, Long mId) {
            return dataMap.getOrDefault(new IdPair(aId, mId), List.of()).size();
        }

        public int getCountByAuthor(Long aId) {
            int sum = 0;
            for (Long mId : mIds) {
                sum += getCount(aId, mId);
            }
            return sum;
        }

        public int getCountByManager(Long mId) {
            int sum = 0;
            for (Long aId : aIds) {
                sum += getCount(aId, mId);
            }
            return sum;
        }

        public int getTotalCount() {
            return this.totalSum;
        }

        private record IdPair(Long authorId, Long managerId) {}
    }

}