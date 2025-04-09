package com.example.SCDProiectv2.Services;

import com.example.SCDProiectv2.DTOs.PackageCreateRequestDTO;
import com.example.SCDProiectv2.DTOs.PackageDetailsDTO;
import com.example.SCDProiectv2.DTOs.PackageEditDTO;
import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.DeliveryPackage;
import com.example.SCDProiectv2.Models.PackageStatus;
import com.example.SCDProiectv2.Models.Product;
import com.example.SCDProiectv2.Repositories.DeliveryPackageRepository;
import com.example.SCDProiectv2.Repositories.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeliveryPackageService {

    @Autowired
    private DeliveryPackageRepository packageRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public DeliveryPackage placeOrder(Set<Product> products, String deliveryAddress, String email, String phoneNumber) {

        DeliveryPackage newPackage = new DeliveryPackage();
        //newPackage.setCourier();
        newPackage.setDeliveryAddress(deliveryAddress);
        newPackage.setEmail(email);
        newPackage.setPhoneNumber(phoneNumber);
        newPackage.setStatus(PackageStatus.NEW);
        //newPackage.setProducts(products);

        return packageRepository.save(newPackage);
    }

    public List<DeliveryPackage> findAllPackages() {
        return packageRepository.findAll();
    }

    @Transactional
    public ResponseEntity<?> deletePackageById(Integer packageId) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String managerName = auth.getName();
            Optional<DeliveryPackage> myPackage = packageRepository.findById(packageId);
            if(myPackage.isPresent()) {
                packageRepository.delete(myPackage.get());
                return ResponseEntity.status(HttpStatus.OK).body("Package deleted successfully");
            }else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package not found");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public List<DeliveryPackage> findPackagesByCourierId(Integer courierId) {
        return packageRepository.findByCourierId(courierId);
    }

    public List<PackageDetailsDTO> findPackagesByStatus(PackageStatus status) {
        List<DeliveryPackage> packages = packageRepository.findByStatus(status);
        List<PackageDetailsDTO> packageDTOs = new ArrayList<>();
        for (DeliveryPackage pkg : packages) {
            packageDTOs.add(new PackageDetailsDTO(pkg));
        }
        return packageDTOs;
    }
    //De aici fac iar ca m-am enervat pe mine ala din trecut de a facut prostii

    @Transactional
    public String createDeliveryPackage(PackageCreateRequestDTO deliveryPackageDTO) {
        try{
            //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            //String managerName = auth.getName();
            String awbNumber = generateAWBNumber();
            DeliveryPackage myPackage = new DeliveryPackage();
            myPackage.setDeliveryAddress(deliveryPackageDTO.getDeliveryAddress());
            myPackage.setEmail(deliveryPackageDTO.getEmail());
            myPackage.setPhoneNumber(deliveryPackageDTO.getPhoneNumber());
            myPackage.setCreatedOn(LocalDateTime.now());
            myPackage.setStatus(PackageStatus.NEW);
            myPackage.setAwbNumber(awbNumber);
            System.out.println(awbNumber);
            packageRepository.save(myPackage);


            /*String subject = "Package Order Confirmation";
            String messageText = "Your package has been successfully created. Your AWB number is: " + awbNumber;
            emailService.sendEmail(deliveryPackageDTO.getEmail(), subject, messageText);
            */
            //return ResponseEntity.status(HttpStatus.CREATED).body("Package created successfully");
            return awbNumber;
        } catch (Exception e){
            return "ERROR FROM BACKEND ---------------->" + e.getMessage();
        }
    }

    public ResponseEntity<?> findPackageByCourierUsername(String username) {
        try {
            List<DeliveryPackage> deliveries = packageRepository.findByCourierId(Integer.parseInt(username));
            List<PackageDetailsDTO> packageDTOs = new ArrayList<>();
            for (DeliveryPackage pkg : deliveries) {
                packageDTOs.add(new PackageDetailsDTO(pkg));
            }
            return ResponseEntity.status(HttpStatus.OK).body(packageDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public List<PackageDetailsDTO> findCurrentCourierDeliveries(){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<Courier> courier = courierRepository.findByUsername(username);
            if (courier.isPresent()) {
                List<DeliveryPackage> deliveries = packageRepository.findByCourierId(courier.get().getId());
                List<PackageDetailsDTO> packageDTOs = new ArrayList<>();
                for (DeliveryPackage pkg : deliveries) {
                    packageDTOs.add(new PackageDetailsDTO(pkg));
                }
                return packageDTOs;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public ResponseEntity<?> changeDeliveryStatus(String address, PackageStatus status) {

        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Optional<Courier> courier = courierRepository.findByUsername(username);
            Optional<DeliveryPackage> myPackage = packageRepository.findByDeliveryAddress(address);
            if(courier.isPresent()) {
                if(myPackage.isPresent()) {
                    myPackage.get().setStatus(status);
                    myPackage.get().setCourier(courier.get());
                    packageRepository.save(myPackage.get());
                    System.out.println(username + "a schimbat statusul pachetului in " +status);
                    return ResponseEntity.status(HttpStatus.OK).body("Package taken successfully");
                } else{
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package not found");
                }

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Courier not found");
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public String generateAWBNumber() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }


    public DeliveryPackage findPackageByAWB(String awbNumber) {
        Optional<DeliveryPackage> myPackage = packageRepository.findByAwbNumber(awbNumber);
        return myPackage.orElse(null);
    }

    public ResponseEntity<?> deletePackageByAWB(String awbNumber) {
        Optional<DeliveryPackage> myPackage = packageRepository.findByAwbNumber(awbNumber);
        if(myPackage.isPresent()) {
            packageRepository.delete(myPackage.get());
            return ResponseEntity.status(HttpStatus.OK).body("Package deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package not found");

    }

    public ResponseEntity<?> editPackageByAWB(PackageEditDTO packageEditDTO){
        Optional<DeliveryPackage> myPackage = packageRepository.findByAwbNumber(packageEditDTO.getAwbNumber());

        if(myPackage.isPresent()) {
            LocalDateTime creationTime = myPackage.get().getCreatedOn();
            System.out.println(creationTime);
            if (creationTime != null && creationTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot edit the package. More than 5 minutes have passed since its creation.");
            }
            myPackage.get().setDeliveryAddress(packageEditDTO.getDeliveryAddress());
            System.out.println(myPackage.get().getDeliveryAddress()+ " ASTA E ADRESA DE INTRA");
            packageRepository.save(myPackage.get());
            return ResponseEntity.status(HttpStatus.OK).body("Package edited successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package not found");
    }
}
