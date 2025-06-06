package com.spring.teststock.controllerrrrr.api;


import com.spring.teststock.dto.ChangerMotDePasseUtilisateurDto;
import com.spring.teststock.dto.UtilisateurDto;
import io.swagger.annotations.Api;

import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.spring.teststock.utils.Constants.UTILISATEUR_ENDPOINT;

@Api("utilisateurs")
public interface UtilisateurApi {

  @PostMapping(UTILISATEUR_ENDPOINT + "/create")
  UtilisateurDto save(@RequestBody UtilisateurDto dto);

  @PostMapping(UTILISATEUR_ENDPOINT + "/update/password")
  UtilisateurDto changerMotDePasse(@RequestBody ChangerMotDePasseUtilisateurDto dto);

  @GetMapping(UTILISATEUR_ENDPOINT + "/{idUtilisateur}")
  UtilisateurDto findById(@PathVariable("idUtilisateur") Integer id);

  @GetMapping(UTILISATEUR_ENDPOINT + "/find/{email}")
  UtilisateurDto findByEmail(@PathVariable("email") String email);

  @GetMapping(UTILISATEUR_ENDPOINT + "/all")
  List<UtilisateurDto> findAll();

  @DeleteMapping(UTILISATEUR_ENDPOINT + "/delete/{idUtilisateur}")
  @Transactional
  void delete(@PathVariable("idUtilisateur") Integer id);

}
