package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.rest.AttachmentDto;
import by.forward.forward_system.core.dto.websocket.WSAttachment;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.repository.AttachmentRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@AllArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private final static Path filesDerictoryPath = Path.of("./files");

    public List<Long> getAttachment(List<WSAttachment> wsAttachment) {
        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        for (WSAttachment attachment : wsAttachment) {
            AttachmentEntity attachmentEntity = attachmentRepository.findById(attachment.getFileAttachmentId()).orElseThrow(() -> new RuntimeException("Not found attachment with id " + attachment.getFileAttachmentId()));
            attachmentEntities.add(attachmentEntity);
        }
        return attachmentEntities.stream()
            .map(AttachmentEntity::getId)
            .toList();
    }

    public List<Long> saveAttachment(List<AttachmentDto> attachmentDtos) {
        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        for (AttachmentDto attachmentDto : attachmentDtos) {
            String filePath = saveWSAttachment(attachmentDto);
            AttachmentEntity attachmentEntity = new AttachmentEntity();
            attachmentEntity.setFilepath(filePath);
            attachmentEntity.setFilename(attachmentDto.getFileName());
            attachmentEntities.add(attachmentRepository.save(attachmentEntity));
        }
        return attachmentEntities.stream().map(AttachmentEntity::getId).toList();
    }

    @SneakyThrows
    private String saveWSAttachment(AttachmentDto attachment) {
        byte[] decode = Base64.getDecoder().decode(attachment.getBase64content());
        boolean exists = Files.exists(filesDerictoryPath);
        if (!exists) {
            Files.createDirectory(filesDerictoryPath);
        }
        String fileName = UUID.randomUUID().toString() + " " + attachment.getFileName();
        Path filePath = Path.of(filesDerictoryPath.toAbsolutePath().toString(), fileName);
        Files.write(filePath, decode);
        return filePath.toString();
    }


}
