package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.rest.AttachmentDto;
import by.forward.forward_system.core.dto.websocket.WSAttachment;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.repository.AttachmentRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class AttachmentService {

    private final static String bucketName = "forward-system-file-storage";
    private final static Path filesDerictoryPath = Path.of("cloud-yandex");
    private final AttachmentRepository attachmentRepository;
    private final AmazonS3 amazonS3;

    public List<Long> getAttachment(List<WSAttachment> wsAttachment) {
        List<Long> ids = wsAttachment.stream().map(WSAttachment::getFileAttachmentId).toList();

        return attachmentRepository.findAllById(ids).stream()
            .map(AttachmentEntity::getId)
            .toList();
    }

    public List<Long> saveAttachment(List<AttachmentDto> attachmentDtos) {
        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        for (AttachmentDto attachmentDto : attachmentDtos) {
            AttachmentEntity attachmentEntity = saveWSAttachment(attachmentDto);
            attachmentEntities.add(attachmentEntity);
        }
        return attachmentEntities.stream().map(AttachmentEntity::getId).toList();
    }

    public Long saveAttachmentRaw(String filename, byte[] content) {
        return saveAttachment(filename, content).getId();
    }

    @SneakyThrows
    public AttachmentEntity saveAttachment(String filenameRaw, byte[] content) {
        String filename = filenameRaw.trim().replaceAll("\\s+", "_");

        UUID fileUUID = UUID.randomUUID();
        String filepath = fileUUID + "___" + filename;
        Path filePath = Path.of(filesDerictoryPath.toString(), filepath);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
            fileUUID.toString(),
            new ByteArrayInputStream(content),
            new ObjectMetadata()
        );

        amazonS3.putObject(putObjectRequest);

        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setFilename(filename);
        attachmentEntity.setObjectKey(fileUUID.toString());
        attachmentEntity.setFilepath(filePath.toString());

        return attachmentRepository.save(attachmentEntity);
    }

    @SneakyThrows
    private AttachmentEntity saveWSAttachment(AttachmentDto attachment) {
        byte[] decode = Base64.getDecoder().decode(attachment.getBase64content());
        String fileName = attachment.getFileName();
        return saveAttachment(fileName, decode);
    }

    @SneakyThrows
    public AttachmentFile loadAttachment(Long attachmentId) {
        AttachmentEntity attachmentEntity = attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Not found attachment with id " + attachmentId));

        S3Object object = amazonS3.getObject(new GetObjectRequest(bucketName, attachmentEntity.getObjectKey()));
        byte[] byteArray = IOUtils.toByteArray(object.getObjectContent());

        return new AttachmentFile(attachmentEntity.getFilename(), attachmentEntity.getFilepath(), byteArray);
    }

    public record AttachmentFile(String filename,
                                 String filepath,
                                 byte[] content) {

    }
}
