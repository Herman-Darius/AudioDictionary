package com.example.SCDProiectv2.Controllers;

import com.example.SCDProiectv2.DTOs.PackageCreateRequestDTO;
import com.example.SCDProiectv2.DTOs.PackageEditDTO;
import com.example.SCDProiectv2.Models.DeliveryPackage;
import com.example.SCDProiectv2.Models.PackageStatus;
import com.example.SCDProiectv2.Services.DeliveryPackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/package")
@CrossOrigin(origins = "http://localhost:8080")
public class PackageController {

    private final DeliveryPackageService packageService;

    public PackageController(DeliveryPackageService packageService) {
        this.packageService = packageService;
    }


    @PostMapping("create")
    public ResponseEntity<?> createDelivery(@RequestBody PackageCreateRequestDTO packageDTO) {
        System.out.println(packageDTO.toString());

        String awbNumber = packageService.createDeliveryPackage(packageDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Package created successfully");
        response.put("awbNumber", awbNumber);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("get-all")
    public List<DeliveryPackage> getAllPackages(){
        return packageService.findAllPackages();
    }

    @GetMapping("/my-deliveries")
    public ResponseEntity<?> getMyDeliveries(){
        return ResponseEntity.ok().body(packageService.findCurrentCourierDeliveries());
    }

    @GetMapping("/new-deliveries")
    public ResponseEntity<?> getNewDeliveries(){
        return ResponseEntity.ok().body(packageService.findPackagesByStatus(PackageStatus.NEW));
    }

    @PostMapping("accept-delivery/{deliveryAddress}")
    public ResponseEntity<?> acceptDelivery(@PathVariable String deliveryAddress) {

        return ResponseEntity.ok().body(packageService.changeDeliveryStatus(deliveryAddress,PackageStatus.PENDING));
    }

    @PostMapping("complete-delivery/{deliveryAddress}")
    public ResponseEntity<?> completeDelivery(@PathVariable String deliveryAddress) {
        return ResponseEntity.ok().body(packageService.changeDeliveryStatus(deliveryAddress,PackageStatus.DELIVERED));
    }

    @GetMapping("/get-order-details/{awbNumber}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String awbNumber) {
        System.out.println("S-A APELAT GET-ORDER-DETAILS");
        DeliveryPackage deliveryPackage = packageService.findPackageByAWB(awbNumber);

        if (deliveryPackage != null) {
            return ResponseEntity.ok(deliveryPackage);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package not found.");
        }
    }
    @DeleteMapping("delete/{awbNumber}")
    public ResponseEntity<?> deleteDelivery(@PathVariable String awbNumber) {
        System.out.println("S-A APELAT DELETE-DELIVERY");
        return ResponseEntity.ok().body(packageService.deletePackageByAWB(awbNumber));
    }

    @PostMapping("edit/{awbNumber}")
    public ResponseEntity<?> editDelivery(@PathVariable String awbNumber, @RequestBody PackageEditDTO packageEditDTO) {
        System.out.println(packageEditDTO.toString());

        return ResponseEntity.ok().body(packageService.editPackageByAWB(packageEditDTO));
    }
}
