package com.example.SCDProiectv2.Services;

import com.example.SCDProiectv2.DTOs.CourierDTO;
import com.example.SCDProiectv2.DTOs.CourierSearchDTO;
import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.Role;
import com.example.SCDProiectv2.Repositories.CourierRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service

public class CourierService {
    @Autowired
    private CourierRepository courierRepository;

    @Transactional
    public Courier assignCourierToAdmin(Integer courierId, Integer adminId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found with ID: " + courierId));
        Courier admin = courierRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole().toString())) {
            throw new RuntimeException("User with ID " + adminId + " is not an admin!");
        }

        courier.setManager(admin);
        return courierRepository.save(courier);
    }
    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }
    public List<CourierDTO> getAllCourierByRole(String role){
        /*Role roleEnum = Role.valueOf(role.toUpperCase());
        return courierRepository.findAllByRole(roleEnum);*/
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<Courier> manager = courierRepository.findByUsername(username);

        Role roleEnum = Role.valueOf(role.toUpperCase());
        return courierRepository.findAllByRole(roleEnum).stream()
                .filter(courier -> !courier.getUsername().equals(username))
                .map(CourierDTO::new) // Automatically maps each entity to a DTO using the constructor
                .toList();
    }
    public Set<CourierSearchDTO> getCouriersManagedByAdmin(Integer adminId) {
        Courier admin = courierRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));

        return admin.getCouriers().stream()
                .map(courier -> new CourierSearchDTO(courier.getId(), courier.getUsername()))
                .collect(Collectors.toSet());
    }
    public ResponseEntity<?> deleteCourier(String username) {
        try{
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if (courier.isPresent()) {
                courierRepository.delete(courier.get());
                return ResponseEntity.status(HttpStatus.OK).body("User "+username+" successfully deleted!");
            } else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User "+username+" not found!");
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }
    public ResponseEntity<?> getAllCouriersWithManagers(){
        Optional<List<Courier>> couriers = courierRepository.findAllWithManagers();
        if(couriers.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(couriers.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");
    }

    public ResponseEntity<?> findCourierByIdAndHasManager(Integer courierId){
        Optional<Courier> courier = courierRepository.findByIdWithManager(courierId);
        if(courier.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(courier.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");
    }

    public ResponseEntity<?> updateCourier(CourierDTO courierDTO) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if(courier.isPresent()) {
                if(courierDTO.getUsername() != null) {
                    courier.get().setUsername(courierDTO.getUsername());
                }
                if(courierDTO.getPassword() != null) {
                    courier.get().setPassword(courierDTO.getPassword());
                }
                if(courierDTO.getEmail() != null) {
                    courier.get().setEmail(courierDTO.getEmail());
                }
                if(courierDTO.getFirstName() != null){
                    courier.get().setFirstName(courierDTO.getFirstName());
                }
                if(courierDTO.getLastName() != null){
                    courier.get().setLastName(courierDTO.getLastName());
                }
                courierRepository.save(courier.get());
                return ResponseEntity.status(HttpStatus.OK).body("Courier updated successfully!");
            } else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> promoteToManager(String username){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String managerName = authentication.getName();
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if(courier.isPresent()) {
                courier.get().setRole(Role.ADMIN);
                courierRepository.save(courier.get());
                return ResponseEntity.status(HttpStatus.OK).body("Courier has been promoted to manager successfully!");
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> demodeFromManager(String username){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String managerName = authentication.getName();
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if(courier.isPresent()) {
                courier.get().setRole(Role.USER);
                courierRepository.save(courier.get());
                return ResponseEntity.status(HttpStatus.OK).body("Courier has been demoted from manager successfully!");
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> takeManagementOverCourier(String username){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String managerName = authentication.getName();
            Optional<Courier> manager = courierRepository.findByUsername(managerName);
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if(courier.isPresent() && manager.isPresent()) {
                if(courier.get().getRole() == Role.USER || courier.get().getManager() == null) {
                    courier.get().setManager(manager.get());
                    courierRepository.save(courier.get());
                    return ResponseEntity.status(HttpStatus.OK).body("Management over" + courier.get().getUsername() +"has been taken successfully!");
                }
                else{
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This courier has already a manager or can't be taken!.");
                }

            } else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<?> revokeManagementOverCourier(String username){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String managerName = authentication.getName();
            Optional<Courier> manager = courierRepository.findByUsername(managerName);
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if(courier.isPresent() && manager.isPresent()) {
                if(courier.get().getManager().equals(manager.get())) {
                    courier.get().setManager(null);
                    courierRepository.save(courier.get());
                    return ResponseEntity.status(HttpStatus.OK).body("Management over" + courier.get().getUsername() +"has been revoked!");
                }
                else
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You can't revoke someone you are not managing.");

            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Courier not found.");
            }

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }



}
