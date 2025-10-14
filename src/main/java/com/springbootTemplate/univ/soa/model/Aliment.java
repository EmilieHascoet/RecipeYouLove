package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Aliment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;

    @ElementCollection(targetClass = CategorieAliment.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "aliment_categories",
            joinColumns = @JoinColumn(name = "aliment_id")
    )
    @Column(name = "categorie")
    private List<CategorieAliment> categories = new ArrayList<>();

    // Constructeur vide
    public  Aliment() {
    }

    // Constructeur complet
    public Aliment(String nom, List<CategorieAliment> categories) {
        this.nom = nom;
        this.categories = categories;
    }
}
