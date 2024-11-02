package by.forward.forward_system.core.services.newchat;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;

public interface QueryHandler<R, T> {
    String getQuery(R request);
    PreparedStatementSetter getPreparedStatementSetter(R request);
    RowMapper<T> getRowMapper();
}
