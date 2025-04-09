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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "root_id", nullable = false)
    private WordRoot root;

    @Column
    private String audioFile;

    @Column(nullable = false, name = "explication")
    private String explication;

}
