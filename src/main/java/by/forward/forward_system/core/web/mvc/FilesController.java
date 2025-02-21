package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UserShortUiDto;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Controller
@AllArgsConstructor
public class FilesController {

    private final AttachmentService attachmentService;
    private final UserUiService userUiService;

    @GetMapping(value = "/load-file/{fileId}")
    @SneakyThrows
    public Object loadFile(@PathVariable("fileId") Long fileId,
                           @RequestParam(value = "download", defaultValue = "false") Boolean download,
                           Model model) {
        if (download) {
            return new RedirectView("/download-file/" + fileId);
        }

        UserShortUiDto currentUserOrAnonymous = userUiService.getCurrentUserOrAnonymous();
        model.addAttribute("userShort", currentUserOrAnonymous);

        if (currentUserOrAnonymous == null) {
            model.addAttribute("authorizedUser", true);
        }

        Optional<String> filename = attachmentService.getFilename(fileId);
        Optional<String> fileExtension = filename.map(name -> FilenameUtils.getExtension(name));

        boolean isMicrosoftFile = fileExtension.map(this::isMicrosoftFile).orElse(false);
        boolean isImage = fileExtension.map(this::isImage).orElse(false);
        boolean fileNotFound = filename.isEmpty();

        if (!fileNotFound && !(isMicrosoftFile || isImage)) {
            return new RedirectView("/download-file/" + fileId);
        }

        model.addAttribute("filename", filename.orElse("Файл не найден."));
        model.addAttribute("isMicrosoftFile", isMicrosoftFile);
        model.addAttribute("isImage", isImage);
        model.addAttribute("fileNotFound", fileNotFound);
        model.addAttribute("fileId", fileId);

        return "main/files/file-view";
    }

    @GetMapping(value = "/download-file/{fileId}")
    @SneakyThrows
    public ResponseEntity<Resource> loadFile(@PathVariable("fileId") Long fileId) {
        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(fileId);

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(attachmentFile.filepath());
        String filename = attachmentFile.filename().substring(attachmentFile.filename().indexOf(' ') + 1);

        return asResponseEntity(new ByteArrayResource(attachmentFile.content()), filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    @GetMapping(value = "/load-server-file/expert-form")
    @SneakyThrows
    public ResponseEntity<Resource> loadExpertForm() {
        Resource resource = new ClassPathResource("/static/expert-form/expert-form.xlsx");

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(resource.getFile().toPath().toString());
        String filename = "Форма рецензии.xlsx";

        return asResponseEntity(resource, filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
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

    private final Set<String> IMAGE_EXTENSION = Set.of(
        "apng",
        "avif",
        "bmp",
        "gif",
        "ico",
        "jpeg",
        "jpg",
        "png",
        "svg",
        "tif",
        "tiff",
        "webp"
    );

    private final Set<String> MICROSOFT_EXTENSION = Set.of(
        "docx",
        "pptx",
        "xlsx",
        "xls",
        "doc",
        "ppt",
        "pdf"
    );

    private boolean isImage(String extension) {
        return IMAGE_EXTENSION.contains(extension);
    }

    private boolean isMicrosoftFile(String extension) {
        return MICROSOFT_EXTENSION.contains(extension);
    }
}
