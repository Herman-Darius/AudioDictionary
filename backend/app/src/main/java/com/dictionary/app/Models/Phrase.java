package com.dictionary.app.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "content")
    private String content;

    @Column
    private String definition;

    @Column
    private String audioFile;

    @ManyToOne
    @JoinColumn(name = "root_id")
    @JsonIgnore
    private WordRoot root;

    @ManyToOne
    @JoinColumn(name = "word_id")
    @JsonIgnore
    private Word word;


}
