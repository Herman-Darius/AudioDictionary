package com.example.SCDProiectv2.DTOs;

import com.example.SCDProiectv2.Models.Courier;
import lombok.*;

@Data
public class CourierDTO {
    //private Integer id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String managerUsername;
    private String role;

    public CourierDTO(Courier courier) {
        this.firstName = courier.getFirstName();
        this.lastName = courier.getLastName();
        this.username = courier.getUsername();
        this.email = courier.getEmail();
        this.managerUsername = courier.getManager() != null ? courier.getManager().getUsername() : "None";
        this.role = courier.getRole().toString();
    }


}
