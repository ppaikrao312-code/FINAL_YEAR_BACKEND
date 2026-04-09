package com.digitalfir.service;

import com.digitalfir.backend.model.FIR;
import com.digitalfir.backend.model.FirStatus;
import com.digitalfir.backend.model.Role;
import com.digitalfir.backend.model.User;
import com.digitalfir.repository.FIRRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FIRService {

    @Autowired
    private FIRRepository firRepository;

    @Autowired
    private UserService userService;

    // ================= CREATE FIR (CITIZEN ONLY) =================
    public FIR createFIR(FIR fir, String userEmail) {

    	User user = userService.getUserByEmail(userEmail);


        if (user.getRole() != Role.CITIZEN) {
            throw new RuntimeException("Only Citizen can create FIR");
        }

        fir.setCreatedBy(user.getId());
        fir.setStatus(FirStatus.SUBMITTED); // default status

        return firRepository.save(fir);
    }

    // ================= GET MY FIRs (CITIZEN) =================
    public List<FIR> getFirsByUser(String userEmail) {

    	User user = userService.getUserByEmail(userEmail);

        if (user.getRole() != Role.CITIZEN) {
            throw new RuntimeException("Only Citizen can view their FIRs");
        }

        return firRepository.findByCreatedBy(user.getId());
    }

    // ================= GET ALL FIRs (POLICE / ADMIN) =================
    public List<FIR> getAllFirs(String userEmail) {

    	User user = userService.getUserByEmail(userEmail);


        if (user.getRole() == Role.CITIZEN) {
            throw new RuntimeException("Citizen cannot view all FIRs");
        }

        return firRepository.findAll();
    }

    // ================= UPDATE FIR STATUS =================
    public FIR updateFIRStatus(Long firId, FirStatus newStatus, String userEmail) {

    	User user = userService.getUserByEmail(userEmail);

        FIR fir = firRepository.findById(firId)
                .orElseThrow(() -> new RuntimeException("FIR not found"));

        // 🚫 Citizen rule
     // 🚫 Citizen cannot update status at all
        if (user.getRole() == Role.CITIZEN) {
            throw new RuntimeException("Citizen is not allowed to update FIR status");
        }

        

        // 👮 Police rule
        if (user.getRole() == Role.POLICE) {
            if (newStatus != FirStatus.UNDER_REVIEW &&
                newStatus != FirStatus.APPROVED &&
                newStatus != FirStatus.REJECTED) {
                throw new RuntimeException("Police cannot set this status");
            }
        }

        // 🛡️ Admin rule
        if (user.getRole() == Role.ADMIN) {
            if (newStatus != FirStatus.CLOSED) {
                throw new RuntimeException("Admin can only close FIR");
            }
        }

        fir.setStatus(newStatus);
        return firRepository.save(fir);
    }

    // ================= DELETE FIR (POLICE / ADMIN) =================
    public void deleteFIR(Long firId, String userEmail) {

    	User user = userService.getUserByEmail(userEmail);

        if (user.getRole() == Role.CITIZEN) {
            throw new RuntimeException("Citizen cannot delete FIR");
        }

        FIR fir = firRepository.findById(firId)
                .orElseThrow(() -> new RuntimeException("FIR not found"));

        firRepository.delete(fir);
    }
}

