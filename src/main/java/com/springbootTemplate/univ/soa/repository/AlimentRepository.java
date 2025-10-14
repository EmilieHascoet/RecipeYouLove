package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Aliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlimentRepository extends JpaRepository<Aliment, Long> {
}

