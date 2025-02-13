package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.core.AttachmentService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@AllArgsConstructor
public class FilesController {

    private final AttachmentService attachmentService;

    @GetMapping(value = "/load-file/{fileId}")
    @SneakyThrows
    public ResponseEntity<Resource> loadFile(@PathVariable("fileId") Long fileId) {
        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(fileId);

        String mimeType = Files.probeContentType(Path.of(attachmentFile.filepath()));
        String filename = attachmentFile.filename().substring(attachmentFile.filename().indexOf(' ') + 1);

        return asResponseEntity(new ByteArrayResource(attachmentFile.content()), filename, mimeType);
    }

    ResponseEntity<Resource> asResponseEntity(Resource resource, String filename, String mimeType) {
        ContentDisposition contentDisposition = ContentDisposition.builder("contentDisposition")
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.asMediaType(MediaType.parseMediaType(mimeType)))
            .header("Content-Disposition", contentDisposition.toString())
            .body(resource);
    }

    @GetMapping(value = "/load-server-file/expert-form")
    @SneakyThrows
    public ResponseEntity<Resource> loadExpertForm() {
        Resource resource = new ClassPathResource("/static/expert-form/expert-form.xlsx");

        String mimeType = Files.probeContentType(resource.getFile().toPath());
        String filename = "Форма рецензии.xlsx";

        return asResponseEntity(resource, filename, mimeType);
    }
}
