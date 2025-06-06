package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.FournisseurDto;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.exception.InvalidOperationException;
import com.spring.teststock.model.CommandeClient;
import com.spring.teststock.model.Entreprise;
import com.spring.teststock.repository.CommandeFournisseurRepository;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.FournisseurRepository;
import com.spring.teststock.servicesss.FournisseurService;
import com.spring.teststock.validator.FournisseurValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FournisseurServiceImpl implements FournisseurService {

  private FournisseurRepository fournisseurRepository;
  private CommandeFournisseurRepository commandeFournisseurRepository;

  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public FournisseurServiceImpl(FournisseurRepository fournisseurRepository,
                                CommandeFournisseurRepository commandeFournisseurRepository,
                                EntrepriseRepository entrepriseRepository) {
    this.fournisseurRepository = fournisseurRepository;
    this.commandeFournisseurRepository = commandeFournisseurRepository;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public FournisseurDto save(FournisseurDto dto) {
    List<String> errors = FournisseurValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Fournisseur is not valid {}", dto);
      throw new InvalidEntityException("Le fournisseur n'est pas valide", ErrorCodes.FOURNISSEUR_NOT_VALID, errors);
    }

    // Vérification de l'existence de l'entreprise référencée
    if (dto.getIdEntreprise() != null) {
      Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getIdEntreprise());
      if (!entrepriseOpt.isPresent()) {
        log.warn("Entreprise with ID {} was not found in the DB", dto.getIdEntreprise());
        throw new EntityNotFoundException("Aucune entreprise avec l'ID " + dto.getIdEntreprise() + " n'a été trouvée dans la BDD",
                ErrorCodes.ENTREPRISE_NOT_FOUND);
      }
    } else {
      throw new InvalidEntityException("L'ID de l'entreprise est requis pour le fournisseur", ErrorCodes.ENTREPRISE_NOT_FOUND,
              Collections.singletonList("L'ID de l'entreprise ne peut pas être nul pour le fournisseur."));
    }

    return FournisseurDto.fromEntity(
        fournisseurRepository.save(
            FournisseurDto.toEntity(dto)
        )
    );
  }

  @Override
  public FournisseurDto findById(Integer id) {
    if (id == null) {
      log.error("Fournisseur ID is null");
      return null;
    }
    return fournisseurRepository.findById(id)
        .map(FournisseurDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
            "Aucun fournisseur avec l'ID = " + id + " n' ete trouve dans la BDD",
            ErrorCodes.FOURNISSEUR_NOT_FOUND)
        );
  }

  @Override
  public List<FournisseurDto> findAll() {
    return fournisseurRepository.findAll().stream()
        .map(FournisseurDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    if (id == null) {
      log.error("Fournisseur ID is null");
      return;
    }
    List<CommandeClient> commandeFournisseur = commandeFournisseurRepository.findAllByFournisseurId(id);
    if (!commandeFournisseur.isEmpty()) {
      throw new InvalidOperationException("Impossible de supprimer un fournisseur qui a deja des commandes",
          ErrorCodes.FOURNISSEUR_ALREADY_IN_USE);
    }
    fournisseurRepository.deleteById(id);
  }
}
