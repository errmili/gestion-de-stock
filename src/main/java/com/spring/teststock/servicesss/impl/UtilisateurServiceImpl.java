package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.ChangerMotDePasseUtilisateurDto;
import com.spring.teststock.dto.EntrepriseDto;
import com.spring.teststock.dto.UtilisateurDto;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.exception.InvalidOperationException;
import com.spring.teststock.model.Entreprise;
import com.spring.teststock.model.Roles;
import com.spring.teststock.model.Utilisateur;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.RolesRepository;
import com.spring.teststock.repository.UtilisateurRepository;
import com.spring.teststock.servicesss.UtilisateurService;
import com.spring.teststock.validator.UtilisateurValidator;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
//@Transactional
public class  UtilisateurServiceImpl implements UtilisateurService {

  private UtilisateurRepository utilisateurRepository;
  private PasswordEncoder passwordEncoder;

  private RolesRepository rolesRepository;

  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public UtilisateurServiceImpl(RolesRepository rolesRepository, UtilisateurRepository utilisateurRepository,
      PasswordEncoder passwordEncoder, EntrepriseRepository entrepriseRepository) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
    this.rolesRepository = rolesRepository;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public UtilisateurDto save(UtilisateurDto dto) {
    List<String> errors = UtilisateurValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Utilisateur is not valid {}", dto);
      throw new InvalidEntityException("L'utilisateur n'est pas valide", ErrorCodes.UTILISATEUR_NOT_VALID, errors);
    }

    if(userAlreadyExists(dto.getEmail())) {
      throw new InvalidEntityException("Un autre utilisateur avec le meme email existe deja", ErrorCodes.UTILISATEUR_ALREADY_EXISTS,
          Collections.singletonList("Un autre utilisateur avec le meme email existe deja dans la BDD"));
    }

    // Vérification et gestion de l'entreprise
    if (dto.getEntreprise() != null) {
      // Si l'entreprise existe déjà (a un ID), on la récupère
      if (dto.getEntreprise().getId() != null) {
        Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getEntreprise().getId());
        if (!entrepriseOpt.isPresent()) {
          throw new InvalidEntityException("L'entreprise référencée n'existe pas.", ErrorCodes.ENTREPRISE_NOT_FOUND,
                  Collections.singletonList("L'entreprise référencée dans l'utilisateur n'existe pas."));
        }
        // Si l'entreprise existe déjà, on l'affecte à l'utilisateur
        dto.setEntreprise(EntrepriseDto.fromEntity(entrepriseOpt.get()));
      } else {
        // Si l'entreprise n'a pas d'ID, cela signifie qu'elle est nouvelle
        Entreprise entreprise = EntrepriseDto.toEntity(dto.getEntreprise());
        // Sauvegarder l'entreprise avant d'affecter à l'utilisateur
        entreprise = entrepriseRepository.save(entreprise);
        // Mettre à jour l'utilisateur avec la nouvelle entreprise
        dto.setEntreprise(EntrepriseDto.fromEntity(entreprise));
      }
    }

    // Convertir le DTO en entité Utilisateur
    Utilisateur utilisateur = UtilisateurDto.toEntity(dto);

    // Ajouter la date de création uniquement dans l'entité Utilisateur
  //  utilisateur.setCreationDate(Instant.now());

    // Encoder le mot de passe
    utilisateur.setMoteDePasse(passwordEncoder.encode(utilisateur.getMoteDePasse()));

    // Sauvegarder l'utilisateur dans la base de données
    return UtilisateurDto.fromEntity(
            utilisateurRepository.save(utilisateur)
    );
  }

  private boolean userAlreadyExists(String email) {
    Optional<Utilisateur> user = utilisateurRepository.findUtilisateurByEmail(email);
    return user.isPresent();
  }

  @Override
  public UtilisateurDto findById(Integer id) {
    if (id == null) {
      log.error("Utilisateur ID is null");
      return null;
    }
    return utilisateurRepository.findById(id)
        .map(UtilisateurDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
            "Aucun utilisateur avec l'ID = " + id + " n' ete trouve dans la BDD",
            ErrorCodes.UTILISATEUR_NOT_FOUND)
        );
  }

  @Override
  public List<UtilisateurDto> findAll() {
    return utilisateurRepository.findAll().stream()
        .map(UtilisateurDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    if (id == null) {
      log.error("Utilisateur ID is null");
      return;
    }

//    // Supprimer les rôles associés à l'utilisateur
//    List<Roles> roles = rolesRepository.findByUtilisateurId(id);
//    if (roles != null && !roles.isEmpty()) {
//      rolesRepository.deleteAll(roles);
//    }

    utilisateurRepository.deleteById(id);
  }

  @Override
  public UtilisateurDto findByEmail(String email) {
    return utilisateurRepository.findUtilisateurByEmail(email)
        .map(UtilisateurDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
        "Aucun utilisateur avec l'email = " + email + " n' ete trouve dans la BDD",
        ErrorCodes.UTILISATEUR_NOT_FOUND)
    );
  }

  @Override
  public UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto) {
    validate(dto);
    Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findById(dto.getId());
    if (utilisateurOptional.isEmpty()) {
      log.warn("Aucun utilisateur n'a ete trouve avec l'ID " + dto.getId());
      throw new EntityNotFoundException("Aucun utilisateur n'a ete trouve avec l'ID " + dto.getId(), ErrorCodes.UTILISATEUR_NOT_FOUND);
    }

    Utilisateur utilisateur = utilisateurOptional.get();
    utilisateur.setMoteDePasse(passwordEncoder.encode(dto.getMotDePasse()));

    return UtilisateurDto.fromEntity(
        utilisateurRepository.save(utilisateur)
    );
  }

  private void validate(ChangerMotDePasseUtilisateurDto dto) {
    if (dto == null) {
      log.warn("Impossible de modifier le mot de passe avec un objet NULL");
      throw new InvalidOperationException("Aucune information n'a ete fourni pour pouvoir changer le mot de passe",
          ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
    }
    if (dto.getId() == null) {
      log.warn("Impossible de modifier le mot de passe avec un ID NULL");
      throw new InvalidOperationException("ID utilisateur null:: Impossible de modifier le mote de passe",
          ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
    }
    if (!StringUtils.hasLength(dto.getMotDePasse()) || !StringUtils.hasLength(dto.getConfirmMotDePasse())) {
      log.warn("Impossible de modifier le mot de passe avec un mot de passe NULL");
      throw new InvalidOperationException("Mot de passe utilisateur null:: Impossible de modifier le mote de passe",
          ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
    }
    if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
      log.warn("Impossible de modifier le mot de passe avec deux mots de passe different");
      throw new InvalidOperationException("Mots de passe utilisateur non conformes:: Impossible de modifier le mote de passe",
          ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
    }
  }
}
