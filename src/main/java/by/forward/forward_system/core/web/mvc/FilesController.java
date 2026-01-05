package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UserShortUiDto;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public Object downloadFile(@PathVariable("fileId") Long fileId, @RequestParam(value = "download", defaultValue = "false") Boolean download, Model model) {

        UserShortUiDto currentUserOrAnonymous = userUiService.getCurrentUserOrAnonymous();
        model.addAttribute("userShort", currentUserOrAnonymous);

        if (currentUserOrAnonymous == null) {
            model.addAttribute("authorizedUser", true);
        }

        Optional<String> filename = attachmentService.getFilename(fileId);
        Optional<String> fileExtension = filename.map(FilenameUtils::getExtension).filter(StringUtils::isNoneBlank).map(String::toLowerCase);

        boolean isMicrosoftFile = fileExtension.map(this::isMicrosoftFile).orElse(false);
        boolean isGoogleFile = fileExtension.map(this::isGoogleFile).orElse(false);
        boolean isImage = fileExtension.map(this::isImage).orElse(false);
        boolean isVideo = fileExtension.map(this::isVideo).orElse(false);
        String mediaType = filename.map(MediaTypeFactory::getMediaType)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(MediaType::asMediaType)
            .map(MediaType::toString)
            .orElse("null");

        boolean fileNotFound = filename.isEmpty();

        if (download || (!fileNotFound && !(isMicrosoftFile || isImage || isGoogleFile || isVideo))) {
            return downloadFile(fileId);
        }

        model.addAttribute("filename", filename.orElse("Файл не найден."));
        model.addAttribute("isMicrosoftFile", isMicrosoftFile);
        model.addAttribute("isGoogleFile", isGoogleFile);
        model.addAttribute("isImage", isImage);
        model.addAttribute("isVideo", isVideo);
        model.addAttribute("mediaType", mediaType);
        model.addAttribute("fileNotFound", fileNotFound);
        model.addAttribute("fileId", fileId);

        return "main/files/file-view";
    }


    @GetMapping(value = "/download-file/{fileId}")
    @SneakyThrows
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) {
        AttachmentService.AttachmentFileInputStream attachmentFile = attachmentService.loadAttachmentAsResource(fileId);

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(attachmentFile.filepath());
        String filename = attachmentFile.filename().substring(attachmentFile.filename().indexOf(' ') + 1);

        return asResponseEntity(
            new InputStreamResource(attachmentFile.inputStream()),
            filename,
            mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM)
        );
    }

    @GetMapping(value = "/load-server-file/expert-form")
    @SneakyThrows
    public ResponseEntity<Resource> loadExpertForm() {
        Resource resource = new ClassPathResource("/static/expert-form/expert-form.xlsx");

        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(resource.getFile().toPath().toString());
        String filename = "Форма рецензии.xlsx";

        return asResponseEntity(resource, filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    @GetMapping(value = "/load-server-file/help")
    @SneakyThrows
    public ResponseEntity<Resource> loadHelpFile() {
        Resource resource = new ClassPathResource("/static/help-file/help-v1.docx");

        String filename = "Памятка для авторов.docx";
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(resource.getFilename());

        return asResponseEntity(resource, filename, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    ResponseEntity<Resource> asResponseEntity(Resource resource, String filename, MediaType mimeType) {
        ContentDisposition contentDisposition = ContentDisposition.builder("contentDisposition")
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.asMediaType(mimeType))
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
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
        "odt",
        "odp",
        "ods",
        "docx",
        "doc",
        "dotm",
        "dotx",
        "xlsx",
        "xlsb",
        "xls",
        "xlsm",
        "ppt",
        "pptx",
        "ppsx",
        "pps",
        "pptm",
        "ppam",
        "potx",
        "ppsm"
    );

    private final Set<String> GOOGLE_EXTENSION = Set.of(
        "pdf",
        "txt",
        "rtf",
        "pages",
        "key",
        "numbers"
    );

    private final Set<String> VIDEO_EXTENSION = Set.of(
        "mp4",
        "webp",
        "ogg",
        "avi",
        "mov",
        "mkv"
    );

    private boolean isVideo(String extension) {
        return VIDEO_EXTENSION.contains(extension);
    }

    private boolean isImage(String extension) {
        return IMAGE_EXTENSION.contains(extension);
    }

    private boolean isMicrosoftFile(String extension) {
        return MICROSOFT_EXTENSION.contains(extension);
    }

    private boolean isGoogleFile(String extension) {
        return GOOGLE_EXTENSION.contains(extension);
    }
}
