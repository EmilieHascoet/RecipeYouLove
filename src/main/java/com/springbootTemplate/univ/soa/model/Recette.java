package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Recette {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String etapes;
    private String tempsTotal;
    private Integer kcal;
    private String image_url;

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL)
    private List<Ingredient> ingredients = new ArrayList<>();

    // Constructeur vide
    public Recette() {
    }

    // Constructeur complet
    public Recette(String nom, String etapes, String tempsTotal, Integer kcal, String image_url, List<Ingredient> ingredients) {
        this.nom = nom;
        this.etapes = etapes;
        this.tempsTotal = tempsTotal;
        this.kcal = kcal;
        this.image_url = image_url;
        this.ingredients = ingredients;
    }
}