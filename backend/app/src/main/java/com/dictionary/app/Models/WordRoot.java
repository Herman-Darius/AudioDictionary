package com.dictionary.app.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "normalized_name")
    private String normalizedName;

    @Column
    private String definition;

    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<Phrase> phrases;

}
