package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.EntrepriseDto;
import com.spring.teststock.dto.RolesDto;
import com.spring.teststock.dto.UtilisateurDto;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.model.Entreprise;
import com.spring.teststock.model.Roles;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.RolesRepository;
import com.spring.teststock.servicesss.EntrepriseService;
import com.spring.teststock.servicesss.UtilisateurService;
import com.spring.teststock.validator.EntrepriseValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(rollbackOn = Exception.class)
@Service
@Slf4j
public class EntrepriseServiceImpl implements EntrepriseService {

  private EntrepriseRepository entrepriseRepository;
  private UtilisateurService utilisateurService;
  private RolesRepository rolesRepository;

  @Autowired
  public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository, UtilisateurService utilisateurService,
                               RolesRepository rolesRepository) {
    this.entrepriseRepository = entrepriseRepository;
    this.utilisateurService = utilisateurService;
    this.rolesRepository = rolesRepository;
  }

  @Override
  public EntrepriseDto save(EntrepriseDto dto) {
    List<String> errors = EntrepriseValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Entreprise is not valid {}", dto);
      throw new InvalidEntityException("L'entreprise n'est pas valide", ErrorCodes.ENTREPRISE_NOT_VALID, errors);
    }

    // Convertir le DTO en entité Entreprise
    Entreprise entreprise = EntrepriseDto.toEntity(dto);

    // Ajouter la date de création à l'entité
   // entreprise.setCreationDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());



//    EntrepriseDto savedEntreprise = EntrepriseDto.fromEntity(
//        entrepriseRepository.save(EntrepriseDto.toEntity(dto))
//    );

    // Sauvegarder l'entité dans la base de données
    Entreprise savedEntreprise = entrepriseRepository.save(entreprise);


    // Convertir l'entité sauvegardée en DTO pour la réponse
    EntrepriseDto savedEntrepriseDto = EntrepriseDto.fromEntity(savedEntreprise);


    // Créer l'utilisateur et le rôle, comme dans votre logique initiale
    UtilisateurDto utilisateur = fromEntreprise(savedEntrepriseDto);
    UtilisateurDto savedUser = utilisateurService.save(utilisateur);

    RolesDto rolesDto = RolesDto.builder()
        .roleName("ADMIN")
        .utilisateur(savedUser)
        .build();

    // Convertir en entité
    Roles roles = RolesDto.toEntity(rolesDto);

    // Définir la date de création sur l'entité
  //  roles.setCreationDate(Instant.now());

    rolesRepository.save(roles);

    // Retourner le DTO de l'entreprise sauvegardée
    return savedEntrepriseDto;
  }

  private UtilisateurDto fromEntreprise(EntrepriseDto dto) {
    return UtilisateurDto.builder()
        .adresse(dto.getAdresse())
        .nom(dto.getNom())
        .prenom(dto.getCodeFiscal())
        .email(dto.getEmail())
        .moteDePasse(generateRandomPassword())
        .entreprise(dto)
        .dateDeNaissance(Instant.now())
        .photo(dto.getPhoto())
        .build();
  }

  private String generateRandomPassword() {
    return "som3R@nd0mP@$$word";
  }

  @Override
  public EntrepriseDto findById(Integer id) {
    if (id == null) {
      log.error("Entreprise ID is null");
      return null;
    }
    return entrepriseRepository.findById(id)
        .map(EntrepriseDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
            "Aucune entreprise avec l'ID = " + id + " n' ete trouve dans la BDD",
            ErrorCodes.ENTREPRISE_NOT_FOUND)
        );
  }

  @Override
  public List<EntrepriseDto> findAll() {
    return entrepriseRepository.findAll().stream()
        .map(EntrepriseDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    if (id == null) {
      log.error("Entreprise ID is null");
      return;
    }
    entrepriseRepository.deleteById(id);
  }
}
