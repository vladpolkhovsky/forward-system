package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.TagDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.enums.TagType;
import by.forward.forward_system.core.jpa.model.TagEntity;
import by.forward.forward_system.core.jpa.repository.TagRepository;
import by.forward.forward_system.core.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional
    public TagEntity create(TagDto createTag) {
        Optional<TagEntity> byNameAndType = tagRepository.findByNameAndType(createTag.getName(), createTag.getType());
        return byNameAndType.orElseGet(() -> {
            TagEntity tag = tagMapper.mapToEntity(createTag);
            return tagRepository.save(tag);
        });
    }

    @Transactional
    public List<TagEntity> crateMany(List<TagDto> tags) {
        return tags.stream().map(this::create).toList();
    }
}
