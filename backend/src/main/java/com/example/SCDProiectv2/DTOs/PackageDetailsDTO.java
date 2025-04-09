package com.example.SCDProiectv2.DTOs;

import com.example.SCDProiectv2.Models.DeliveryPackage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PackageDetailsDTO {
    private LocalDate createdOn;
    private String deliveryAddress;
    private String email;
    private String phoneNumber;
    private boolean payOnDelivery;
    private String status;
    private CourierDTO courier;

    public PackageDetailsDTO(DeliveryPackage deliveryPackage) {
        this.createdOn = LocalDate.from(deliveryPackage.getCreatedOn());
        this.deliveryAddress = deliveryPackage.getDeliveryAddress();
        this.email = deliveryPackage.getEmail();
        this.phoneNumber = deliveryPackage.getPhoneNumber();
        this.payOnDelivery = deliveryPackage.isPayOnDelivery();
        this.status = deliveryPackage.getStatus().toString();
        if (deliveryPackage.getCourier() != null) {
            this.courier = new CourierDTO(deliveryPackage.getCourier());
        }
    }

}
