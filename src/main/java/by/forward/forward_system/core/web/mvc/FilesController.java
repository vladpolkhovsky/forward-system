package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.repository.AttachmentRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@AllArgsConstructor
public class FilesController {

    private final AttachmentRepository attachmentRepository;

    @GetMapping(value = "/load-file/{fileId}")
    @SneakyThrows
    public void loadFile(@PathVariable("fileId") Long fileId, HttpServletResponse httpServletResponse) {
        AttachmentEntity attachmentEntity = attachmentRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));

        Resource resource = new FileSystemResource(Path.of(attachmentEntity.getFilepath()));

        String mimeType = Files.probeContentType(resource.getFile().toPath());
        String filename = resource.getFilename().substring(resource.getFilename().indexOf(' ') + 1);

        ContentDisposition attachment = ContentDisposition.builder("attachment")
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", attachment.toString());

        InputStream inputStream = resource.getInputStream();
        OutputStream outputStream = httpServletResponse.getOutputStream();

        StreamUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }
}
