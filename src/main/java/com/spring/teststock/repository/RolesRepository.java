package com.spring.teststock.repository;


import java.util.List;

import com.spring.teststock.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Integer> {


    List<Roles> findByUtilisateurId(Integer idUtilisateur);

}
