package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.MessageViewStatusView;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageViewStatusRepository extends ListCrudRepository<MessageViewStatusView, Long> {
}
