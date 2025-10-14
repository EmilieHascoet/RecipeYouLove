package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recette_id")
    private Recette recette;

    @ManyToOne
    @JoinColumn(name = "aliment_id")
    private Aliment aliment;

    @Enumerated(EnumType.STRING)
    private Unite unite;

    private Integer quantite;
    private Boolean principal;

    // Constructeur vide
    public Ingredient() {
    }

    // Constructeur complet
    public Ingredient(Long id, Recette recette, Aliment aliment, Unite unite, Integer quantite, Boolean principal) {
        this.id = id;
        this.recette = recette;
        this.aliment = aliment;
        this.unite = unite;
        this.quantite = quantite;
        this.principal = principal;
    }
}
