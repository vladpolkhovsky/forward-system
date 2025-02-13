package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.core.AttachmentService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class FilesController {

    private final AttachmentService attachmentService;

    @GetMapping(value = "/load-file/{fileId}")
    @SneakyThrows
    public ResponseEntity<Resource> loadFile(@PathVariable("fileId") Long fileId) {
        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(fileId);

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(attachmentFile.filepath());
        String filename = attachmentFile.filename().substring(attachmentFile.filename().indexOf(' ') + 1);

        return asResponseEntity(new ByteArrayResource(attachmentFile.content()), filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    ResponseEntity<Resource> asResponseEntity(Resource resource, String filename, MediaType mimeType) {
        ContentDisposition contentDisposition = ContentDisposition.builder("contentDisposition")
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.asMediaType(mimeType))
            .header("Content-Disposition", contentDisposition.toString())
            .body(resource);
    }

    @GetMapping(value = "/load-server-file/expert-form")
    @SneakyThrows
    public ResponseEntity<Resource> loadExpertForm() {
        Resource resource = new ClassPathResource("/static/expert-form/expert-form.xlsx");

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(resource.getFile().toPath().toString());
        String filename = "Форма рецензии.xlsx";

        return asResponseEntity(resource, filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }
}
