package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Recette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Long> {
}

