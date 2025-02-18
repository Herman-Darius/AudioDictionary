package com.dictionary.app.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roots")
public class WordRoot {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;


}
