package com.dictionary.app.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "phrases")
public class Phrase {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String phrase;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;


}
