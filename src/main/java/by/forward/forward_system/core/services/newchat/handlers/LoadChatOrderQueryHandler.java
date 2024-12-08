package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.OrderDatesDto;
import by.forward.forward_system.core.dto.messenger.fast.OrderInfoDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
public class LoadChatOrderQueryHandler implements QueryHandler<Long, OrderInfoDto> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Language("SQL")
    private final static String QUERY = """
        select o.id as id, o.tech_number as techNumber, o.order_status as status, d."name" as discipline, o.deadline as deadline, o.intermediate_deadline as ideadline, o.work_type as workType, o.additional_dates as dates from forward_system.orders o
        	left join forward_system.disciplines d on o.discipline_id = d.id
        	where o.id = ?
        """;

    @Override
    public String getQuery(Long request) {
        return QUERY;
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(Long request) {
        return ps -> {
            ps.setLong(1, request);
        };
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "Не назначен";
        }
        return timestamp.toLocalDateTime().format(dateTimeFormatter);
    }

    @SneakyThrows
    private String parseDate(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        Date date = inputDateFormat.parse(dateString.replace("T", " "));
        return outputDateFormat.format(date);
    }

    @SneakyThrows
    private String formatDates(String dates) {
        if (StringUtils.isBlank(dates)) {
            return "[]";
        } else {
            List<OrderDatesDto> orderDatesDtos = mapper.readValue(dates, new TypeReference<List<OrderDatesDto>>() {

            });
            orderDatesDtos = orderDatesDtos.stream().map(t -> new OrderDatesDto(
                t.getText(),
                parseDate(t.getTime())
            )).toList();
            return mapper.writeValueAsString(orderDatesDtos);
        }
    }

    @Override
    public RowMapper<OrderInfoDto> getRowMapper() {
        return (rs, rowNum) -> {
            OrderInfoDto dto = new OrderInfoDto();

            dto.setId(rs.getLong("id"));
            dto.setTechNumber(rs.getString("techNumber"));
            dto.setOrderStatus(rs.getString("status"));
            dto.setDiscipline(rs.getString("discipline"));
            dto.setDeadline(formatDate(rs.getTimestamp("deadline")));
            dto.setIntermediateDeadline(formatDate(rs.getTimestamp("ideadline")));
            dto.setWorkType(rs.getString("workType"));
            dto.setDates(formatDates(rs.getString("dates")));

            return dto;
        };
    }
}
