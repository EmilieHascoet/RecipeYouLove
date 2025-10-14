package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}

