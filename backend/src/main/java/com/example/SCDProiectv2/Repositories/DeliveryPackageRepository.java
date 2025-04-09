package com.example.SCDProiectv2.Repositories;

import com.example.SCDProiectv2.Models.DeliveryPackage;
import com.example.SCDProiectv2.Models.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPackageRepository extends JpaRepository<DeliveryPackage, Integer> {

    List<DeliveryPackage> findByCourierId(Integer courierId);

    List<DeliveryPackage> findByStatus(PackageStatus status);

    Optional<DeliveryPackage> findByDeliveryAddress(String deliveryAddress);

    Optional<DeliveryPackage> findByCourierIdAndStatus(Integer courierId, String status);

    Optional<DeliveryPackage> findByAwbNumber(String awbNumber);
    //Optional<DeliveryPackage> findByPhoneNumberAndName(String phoneNumber, String name);
}
