package com.example.SCDProiectv2.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PackageCreateRequestDTO {
    private String deliveryAddress;
    private String email;
    private String phoneNumber;
    private String awbNumber;
}