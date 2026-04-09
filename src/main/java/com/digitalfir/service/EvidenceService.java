package com.digitalfir.service;

import com.digitalfir.backend.model.*;
import com.digitalfir.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvidenceService {

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private FIRRepository firRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // ================= GET UPLOAD DIR =================
    private String getUploadDir() {
        String baseDir = System.getenv("UPLOAD_DIR");

        if (baseDir == null || baseDir.isBlank()) {
            baseDir = "uploads";
        }

        return baseDir;
    }

    // ================= UPLOAD =================
    public Evidence uploadEvidence(Long firId,
                                   MultipartFile file,
                                   String email) throws IOException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FIR fir = firRepository.findById(firId)
                .orElseThrow(() -> new RuntimeException("FIR not found"));

        File dir = new File(getUploadDir());
        if (!dir.exists()) dir.mkdirs();

        String fileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();

        File dest = new File(dir, fileName);
        file.transferTo(dest);

        Evidence evidence = new Evidence();
        evidence.setFileName(fileName);
        evidence.setFileType(file.getContentType());
        evidence.setFilePath(dest.getAbsolutePath());
        evidence.setUploadedBy(user.getId());
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setFir(fir);
        evidence.setIsDeleted(false);

        Evidence savedEvidence = evidenceRepository.save(evidence);

        // 🔔 SAFE NOTIFICATION
        if (fir.getCreatedBy() != null) {
            userRepository.findById(fir.getCreatedBy()).ifPresent(firOwner -> {
                notificationService.createNotification(
                        firOwner,
                        "New evidence uploaded for FIR #" + fir.getId(),
                        NotificationType.EVIDENCE_UPLOADED,
                        fir.getId()
                );
            });
        }

        return savedEvidence;
    }

    // ================= GET BY FIR =================
    public List<Evidence> getEvidenceByFir(Long firId, String email) {

        FIR fir = firRepository.findById(firId)
                .orElseThrow(() -> new RuntimeException("FIR not found"));

        return evidenceRepository.findByFirAndIsDeletedFalse(fir);
    }

    // ================= PUBLIC VIEW =================
    public Resource viewEvidencePublic(Long id) {

        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));

        if (Boolean.TRUE.equals(evidence.getIsDeleted())) {
            throw new RuntimeException("Evidence is deleted");
        }

        try {
            Path path = Paths.get(evidence.getFilePath());
            return new UrlResource(path.toUri());
        } catch (Exception e) {
            throw new RuntimeException("File not found");
        }
    }

    // ================= ADMIN VIEW =================
    public Resource viewEvidenceForAdmin(Long id) {

        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));

        try {
            Path path = Paths.get(evidence.getFilePath());
            return new UrlResource(path.toUri());
        } catch (Exception e) {
            throw new RuntimeException("File not found");
        }
    }

    // ================= SOFT DELETE =================
    public void deleteEvidence(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.POLICE && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Not allowed");
        }

        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));

        evidence.setIsDeleted(true);
        evidence.setDeletedAt(LocalDateTime.now());
        evidence.setDeletedBy(user.getRole().name());

        evidenceRepository.save(evidence);
    }
}
