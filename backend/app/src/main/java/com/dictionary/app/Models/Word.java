package com.dictionary.app.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String word;

    @Column
    private String definition;

    /*
    @Column
    private String phrase;

     */

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "root_id", nullable = false)
    private WordRoot root;

    @Column
    private String audioFile;


}
