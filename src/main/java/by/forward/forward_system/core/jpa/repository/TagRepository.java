package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.enums.TagType;
import by.forward.forward_system.core.jpa.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByNameAndType(String name, TagType type);
}
