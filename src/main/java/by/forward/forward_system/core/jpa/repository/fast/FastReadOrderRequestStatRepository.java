package by.forward.forward_system.core.jpa.repository.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class FastReadOrderRequestStatRepository {

    @Language("SQL")
    private static final String LOAD_ALL_REQUEST_DATA_SQL = """
            select distinct ors.id     as id,
                        ors.author_id  as aId,
                        ors.manager_id as mId,
                        ors.order_id   as oId,
                        ors.created_at as createdAt
            from forward_system.order_request_statistics ors
            where created_at >= ?
              and created_at <= ?
        """;
    private final JdbcTemplate jdbcTemplate;

    public List<FastReadOrderRequestStatRepository.StatDto> readAllStatInInterval(LocalDate from, LocalDate to) {
        LocalDateTime toLDT = to.plusDays(1).atStartOfDay().minusSeconds(1);

        Timestamp fromTimestamp = Timestamp.valueOf(from.atStartOfDay());
        Timestamp toTimestamp = Timestamp.valueOf(toLDT);

        return jdbcTemplate.query(LOAD_ALL_REQUEST_DATA_SQL, new Object[]{fromTimestamp, toTimestamp}, new int[]{Types.TIMESTAMP, Types.TIMESTAMP}, (rs, rowNum) -> new StatDto(
            rs.getLong("id"),
            rs.getLong("aId"),
            rs.getLong("mId"),
            rs.getLong("oId"),
            rs.getTimestamp("createdAt").toLocalDateTime().toLocalDate()
        ));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatDto {
        private Long id;
        private Long authorId;
        private Long managerId;
        private Long orderId;
        private LocalDate createdAt;
    }
}
