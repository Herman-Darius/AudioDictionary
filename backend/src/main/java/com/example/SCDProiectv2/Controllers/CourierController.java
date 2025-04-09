package com.example.SCDProiectv2.Controllers;

import com.example.SCDProiectv2.DTOs.CourierDTO;
import com.example.SCDProiectv2.DTOs.CourierSearchDTO;
import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.Role;
import com.example.SCDProiectv2.Repositories.CourierRepository;
import com.example.SCDProiectv2.Services.CourierService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@AllArgsConstructor

public class CourierController {
    private CourierService courierService;

    @GetMapping("/courier/{id}/packages")
    public List<Package> getPackagesForCourier(
            @PathVariable("id") int id
    ){
        return List.of();
    }
    @GetMapping("/courier/get-all-with-manager")
    public ResponseEntity<?> getAllCouriersWithManager() {
        return ResponseEntity.ok().body(courierService.getAllCouriersWithManagers());
    }

    @GetMapping("/courier/get-courier-with-manager/{id}")
    public ResponseEntity<?> getCourierWithManager(@PathVariable Integer id) {
        return ResponseEntity.ok().body(courierService.findCourierByIdAndHasManager(id));
    }

    @PostMapping("/courier/assign")
    public ResponseEntity<?> assignCourierToAdmin(@RequestParam Integer courierId, @RequestParam Integer adminId) {
        Courier updatedCourier = courierService.assignCourierToAdmin(courierId, adminId);
        return ResponseEntity.ok(updatedCourier);
    }
    @GetMapping("/courier/{adminId}/couriers")
    public ResponseEntity<?> getCouriersManagedByAdmin(@PathVariable Integer adminId) {
        Set<CourierSearchDTO> couriers = courierService.getCouriersManagedByAdmin(adminId);
        return ResponseEntity.ok(couriers);
    }
    @GetMapping("/courier/get-all")
    public ResponseEntity<?> getAllCouriers() {
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    @PostMapping("/courier/update")
    public ResponseEntity<?> updateCourier(@RequestBody CourierDTO courierDTO) {
        return ResponseEntity.ok(courierService.updateCourier(courierDTO));
    }

    @DeleteMapping("/courier/delete/{username}")
    public ResponseEntity<?> deleteCourier(@PathVariable String username) {
        return ResponseEntity.ok(courierService.deleteCourier(username));
    }

    @PostMapping("/courier/{username}/promote")
    public ResponseEntity<?> promoteCourier(@PathVariable String username) {
        return ResponseEntity.ok(courierService.promoteToManager(username));
    }

    @PostMapping("/courier/{username}/demote")
    public ResponseEntity<?> demoteCourier(@PathVariable String username) {
        return ResponseEntity.ok(courierService.demodeFromManager(username));
    }

    @GetMapping("/courier/get-all/{role}")
    public ResponseEntity<?> getAllCouriers(@PathVariable String role) {
        return ResponseEntity.ok(courierService.getAllCourierByRole(role));
    }

    @PostMapping("/courier/take-management/{username}")
    public ResponseEntity<?> takeCourier(@PathVariable String username) {
        return ResponseEntity.ok(courierService.takeManagementOverCourier(username));
    }

    @PostMapping("/courier/revoke-management/{username}")
    public ResponseEntity<?> revokeCourier(@PathVariable String username) {
        return ResponseEntity.ok(courierService.revokeManagementOverCourier(username));
    }


}
