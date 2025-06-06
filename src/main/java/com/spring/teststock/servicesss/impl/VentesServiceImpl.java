package com.spring.teststock.servicesss.impl;


import com.spring.teststock.dto.ArticleDto;
import com.spring.teststock.dto.LigneVenteDto;
import com.spring.teststock.dto.MvtStkDto;
import com.spring.teststock.dto.VentesDto;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.exception.InvalidOperationException;
import com.spring.teststock.model.*;
import com.spring.teststock.repository.ArticleRepository;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.LigneVenteRepository;
import com.spring.teststock.repository.VentesRepository;
import com.spring.teststock.servicesss.MvtStkService;
import com.spring.teststock.servicesss.VentesService;
import com.spring.teststock.validator.VentesValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VentesServiceImpl implements VentesService {

  private ArticleRepository articleRepository;
  private VentesRepository ventesRepository;
  private LigneVenteRepository ligneVenteRepository;
  private MvtStkService mvtStkService;

  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public VentesServiceImpl(ArticleRepository articleRepository, VentesRepository ventesRepository,
                           LigneVenteRepository ligneVenteRepository, MvtStkService mvtStkService,
                           EntrepriseRepository entrepriseRepository) {
    this.articleRepository = articleRepository;
    this.ventesRepository = ventesRepository;
    this.ligneVenteRepository = ligneVenteRepository;
    this.mvtStkService = mvtStkService;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public VentesDto save(VentesDto dto) {

    List<String> errors = VentesValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Ventes n'est pas valide");
      throw new InvalidEntityException("L'objet vente n'est pas valide", ErrorCodes.VENTE_NOT_VALID, errors);
    }

    // Vérification de l'entreprise référencée
    if (dto.getIdEntreprise() != null) {
      Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getIdEntreprise());
      if (!entrepriseOpt.isPresent()) {
        throw new InvalidEntityException("L'entreprise référencée n'existe pas.", ErrorCodes.ENTREPRISE_NOT_FOUND,
                Collections.singletonList("L'entreprise référencée dans la vente n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de l'entreprise est requis pour la vente", ErrorCodes.ENTREPRISE_NOT_FOUND,
              Collections.singletonList("L'ID de l'entreprise ne peut pas être nul pour la vente."));
    }

    List<String> articleErrors = new ArrayList<>();

    dto.getLigneVentes().forEach(ligneVenteDto -> {
      Optional<Article> article = articleRepository.findById(ligneVenteDto.getArticle().getId());
      if (article.isEmpty()) {
        articleErrors.add("Aucun article avec l'ID " + ligneVenteDto.getArticle().getId() + " n'a ete trouve dans la BDD");
      }
    });

    if (!articleErrors.isEmpty()) {
      log.error("One or more articles were not found in the DB, {}", errors);
      throw new InvalidEntityException("Un ou plusieurs articles n'ont pas ete trouve dans la BDD", ErrorCodes.VENTE_NOT_VALID, errors);
    }

    Ventes savedVentes = ventesRepository.save(VentesDto.toEntity(dto));

    dto.getLigneVentes().forEach(ligneVenteDto -> {
      LigneVente ligneVente = LigneVenteDto.toEntity(ligneVenteDto);
      ligneVente.setVente(savedVentes);
      ligneVenteRepository.save(ligneVente);
      updateMvtStk(ligneVente);
    });

    return VentesDto.fromEntity(savedVentes);
  }

  @Override
  public VentesDto findById(Integer id) {
    if (id == null) {
      log.error("Ventes ID is NULL");
      return null;
    }
    return ventesRepository.findById(id)
            .map(VentesDto::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Aucun vente n'a ete trouve dans la BDD", ErrorCodes.VENTE_NOT_FOUND));
  }

  @Override
  public VentesDto findByCode(String code) {
    if (!StringUtils.hasLength(code)) {
      log.error("Vente CODE is NULL");
      return null;
    }
    return ventesRepository.findVentesByCode(code)
            .map(VentesDto::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException(
                    "Aucune vente client n'a ete trouve avec le CODE " + code, ErrorCodes.VENTE_NOT_VALID
            ));
  }

  @Override
  public List<VentesDto> findAll() {
    return ventesRepository.findAll().stream()
            .map(VentesDto::fromEntity)
            .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    if (id == null) {
      log.error("Vente ID is NULL");
      return;
    }
    List<LigneVente> ligneVentes = ligneVenteRepository.findAllByVenteId(id);
    if (!ligneVentes.isEmpty()) {
      throw new InvalidOperationException("Impossible de supprimer une vente ...",
              ErrorCodes.VENTE_ALREADY_IN_USE);
    }
    ventesRepository.deleteById(id);
  }

  private void updateMvtStk(LigneVente lig) {
    MvtStkDto mvtStkDto = MvtStkDto.builder()
            .article(ArticleDto.fromEntity(lig.getArticle()))
            .dateMvt(Instant.now())
            .typeMvt(TypeMvtStk.SORTIE)
            .sourceMvt(SourceMvtStk.VENTE)
            .quantite(lig.getQuantite())
            .idEntreprise(lig.getIdEntreprise())
            .build();
    mvtStkService.sortieStock(mvtStkDto);
  }
}



