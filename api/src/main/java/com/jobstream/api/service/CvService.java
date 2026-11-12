package com.jobstream.api.service;

import com.jobstream.api.entity.UserCv;
import com.jobstream.api.exception.CvValidationException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import com.jobstream.api.mapper.CvMapper;
import com.jobstream.api.repository.UserCvRepository;
import com.jobstream.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CvService {
    private static final int MAX_FILE_SIZE_BYTES = 5_000_000;
    private final UserCvRepository cvRepository;
    private final UserRepository userRepository;
    private final CvMapper cvMapper;

    @Transactional
    public CvUploadResult uploadCv(Long userId, MultipartFile file) {
        byte[] content = readPdf(file);
        String filename = filename(file);

        var existing = cvRepository.findByUserId(userId);
        boolean created = existing.isEmpty();

        UserCv cv = existing.orElseGet(() -> {
            UserCv newCv = new UserCv();
            newCv.setUser(userRepository.getReferenceById(userId));
            return newCv;
        });

        cv.setContent(content);
        cv.setOriginalFilename(filename);
        cv.setSizeBytes((long) content.length);
        cv.setUploadedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        cv.setRevision(created ? 1L : cv.getRevision() + 1);

        return new CvUploadResult(cvMapper.toDto(cvRepository.save(cv)), created);
    }
    private byte[] readPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CvValidationException(CvValidationException.Reason.INVALID_FILE, "A non-empty PDF file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CvValidationException(CvValidationException.Reason.TOO_LARGE, "The PDF must not exceed 5 MB");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new CvValidationException(CvValidationException.Reason.UNSUPPORTED_MEDIA_TYPE, "The file must have application/pdf content type");
        }
        try (var input = file.getInputStream()) {
            byte[] content = input.readNBytes(MAX_FILE_SIZE_BYTES + 1);
            if (content.length > MAX_FILE_SIZE_BYTES) {
                throw new CvValidationException(CvValidationException.Reason.TOO_LARGE, "The PDF must not exceed 5 MB");
            }
            try (PDDocument document = Loader.loadPDF(content)) {
                // Load the page tree to check readability, without extracting text.
                document.getNumberOfPages();
            }
            return content;
        } catch (IOException exception) {
            throw new CvValidationException(CvValidationException.Reason.INVALID_FILE,
                    "A readable PDF file is required", exception);
        }
    }

    private String filename(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.codePoints().anyMatch(Character::isISOControl)) {
            throw new CvValidationException(CvValidationException.Reason.INVALID_FILE, "A valid filename is required");
        }
        String normalized = original.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).strip();
        if (name.isEmpty()) {
            throw new CvValidationException(CvValidationException.Reason.INVALID_FILE, "A valid filename is required");
        }
        return name;
    }
}
