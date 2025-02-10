package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.repository.AttachmentRepository;
import by.forward.forward_system.core.services.core.AttachmentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@AllArgsConstructor
public class FilesController {

    private final AttachmentRepository attachmentRepository;

    private final AttachmentService attachmentService;

    @GetMapping(value = "/load-file/{fileId}")
    @SneakyThrows
    public void loadFile(@PathVariable("fileId") Long fileId, HttpServletResponse httpServletResponse) {
        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(fileId);

        String mimeType = Files.probeContentType(Path.of(attachmentFile.filepath()));
        String filename = attachmentFile.filename().substring(attachmentFile.filename().indexOf(' ') + 1);

        ContentDisposition attachment = ContentDisposition.builder("attachment")
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", attachment.toString());

        ByteArrayInputStream fileBytesStream = new ByteArrayInputStream(attachmentFile.content());
        OutputStream outputStream = httpServletResponse.getOutputStream();

        StreamUtils.copy(fileBytesStream, outputStream);
        fileBytesStream.close();
        outputStream.close();
    }

    @GetMapping(value = "/load-server-file/expert-form")
    @SneakyThrows
    public void loadExpertForm(HttpServletResponse httpServletResponse) {
        Resource resource = new ClassPathResource("/static/expert-form/expert-form.xlsx");

        String mimeType = Files.probeContentType(resource.getFile().toPath());
        String filename = "Форма рецензии.xlsx";

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
