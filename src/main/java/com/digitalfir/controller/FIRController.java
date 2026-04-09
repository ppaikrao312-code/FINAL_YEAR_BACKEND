package com.digitalfir.controller;

import com.digitalfir.backend.model.FIR;
import com.digitalfir.backend.model.FirStatus;
import com.digitalfir.backend.model.FirStatusHistory;
import com.digitalfir.service.FIRService;

import lombok.Value;

import com.digitalfir.repository.FirStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fir")
@CrossOrigin(origins = "*")
public class FIRController {
	
	

    @Autowired
    private FIRService firService;

    @Autowired
    private FirStatusHistoryRepository historyRepository;

    // ================= CREATE FIR =================
    @PostMapping("/create")
    public ResponseEntity<FIR> createFIR(@RequestBody FIR fir) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(firService.createFIR(fir, email));
    }

    // ================= GET FIRs =================
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('POLICE','ADMIN')")
    public ResponseEntity<List<FIR>> getAllFirs(Authentication auth) {
        return ResponseEntity.ok(
            firService.getAllFirs(auth.getName())
        );
    }

 // ================= GET MY FIR (CITIZEN) =================
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<FIR>> getMyFirs(Authentication auth) {

        String email = auth.getName();   // logged-in citizen email

        return ResponseEntity.ok(
            firService.getFirsByUser(email)
        );
    }


    // ================= UPDATE STATUS =================
    @PutMapping("/update-status/{id}")
    public ResponseEntity<FIR> updateStatus(
            @PathVariable Long id,
            @RequestParam FirStatus status) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(
                firService.updateFIRStatus(id, status, email)
        );
    }

    // ================= STATUS HISTORY =================
    @GetMapping("/{id}/history")
    public ResponseEntity<List<FirStatusHistory>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(
                historyRepository.findByFirId(id)
        );
    }

    // ================= DELETE FIR =================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFIR(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        firService.deleteFIR(id, email);
        return ResponseEntity.ok("FIR deleted successfully");
    }
}
