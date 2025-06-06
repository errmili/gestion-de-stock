package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.ArticleDto;
import com.spring.teststock.dto.MvtStkDto;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.model.Article;
import com.spring.teststock.model.Entreprise;
import com.spring.teststock.model.MvtStk;
import com.spring.teststock.model.TypeMvtStk;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.MvtStkRepository;
import com.spring.teststock.servicesss.ArticleService;
import com.spring.teststock.servicesss.MvtStkService;
import com.spring.teststock.validator.MvtStkValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MvtStkServiceImpl implements MvtStkService {

  private MvtStkRepository repository;
  private ArticleService articleService;
  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public MvtStkServiceImpl(MvtStkRepository repository, ArticleService articleService, EntrepriseRepository entrepriseRepository) {
    this.repository = repository;
    this.articleService = articleService;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public BigDecimal stockReelArticle(Integer idArticle) {
    if (idArticle == null) {
      log.warn("ID article is NULL");
      return BigDecimal.valueOf(-1);
    }
    articleService.findById(idArticle); // Vérification de l'existence de l'article
    return repository.stockReelArticle(idArticle);
  }

  @Override
  public List<MvtStkDto> mvtStkArticle(Integer idArticle) {
    return repository.findAllByArticleId(idArticle).stream()
            .map(MvtStkDto::fromEntity)
            .collect(Collectors.toList());
  }

  @Override
  public MvtStkDto entreeStock(MvtStkDto dto) {
    return handleStockMovement(dto, TypeMvtStk.ENTREE);
  }

  @Override
  public MvtStkDto sortieStock(MvtStkDto dto) {
    return handleStockMovement(dto, TypeMvtStk.SORTIE);
  }

  @Override
  public MvtStkDto correctionStockPos(MvtStkDto dto) {
    return handleStockMovement(dto, TypeMvtStk.CORRECTION_POS);
  }

  @Override
  public MvtStkDto correctionStockNeg(MvtStkDto dto) {
    return handleStockMovement(dto, TypeMvtStk.CORRECTION_NEG);
  }

  // Méthode générique pour gérer les mouvements de stock (entrée ou sortie)
  //@Override
  private MvtStkDto handleStockMovement(MvtStkDto dto, TypeMvtStk typeMvtStk) {
    // Vérification de la validité du DTO
    List<String> errors = MvtStkValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Mouvement de stock invalide {}", dto);
      throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STK_NOT_VALID, errors);
    }

    // Vérification de l'existence de l'entreprise référencée
    if (dto.getIdEntreprise() != null) {
      Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getIdEntreprise());
      if (!entrepriseOpt.isPresent()) {
        throw new InvalidEntityException("L'entreprise référencée n'existe pas.", ErrorCodes.ENTREPRISE_NOT_FOUND,
                Collections.singletonList("L'entreprise référencée dans le mouvement de stock n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de l'entreprise est requis pour le mouvement de stock", ErrorCodes.ENTREPRISE_NOT_FOUND,
              Collections.singletonList("L'ID de l'entreprise ne peut pas être nul pour le mouvement de stock."));
    }

    // Vérification de l'existence de l'article référencé
    if (dto.getArticle() != null && dto.getArticle().getId() != null) {
      ArticleDto articleOpt = articleService.findById(dto.getArticle().getId());
      if (articleOpt == null) {
        throw new InvalidEntityException("L'article référencé n'existe pas.", ErrorCodes.ARTICLE_NOT_FOUND,
                Collections.singletonList("L'article référencé dans le mouvement de stock n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de l'article est requis pour le mouvement de stock", ErrorCodes.ARTICLE_NOT_FOUND,
              Collections.singletonList("L'ID de l'article ne peut pas être nul pour le mouvement de stock."));
    }

    // Ajuster la quantité selon le type de mouvement
    if (typeMvtStk == TypeMvtStk.SORTIE || typeMvtStk == TypeMvtStk.CORRECTION_NEG) {
      dto.setQuantite(BigDecimal.valueOf(-Math.abs(dto.getQuantite().doubleValue()))); // Quantité négative pour sortie ou correction négative
    } else {
      dto.setQuantite(BigDecimal.valueOf(Math.abs(dto.getQuantite().doubleValue()))); // Quantité positive pour entrée ou correction positive
    }

    // Définir le type de mouvement (ENTREE, SORTIE, etc.)
    dto.setTypeMvt(typeMvtStk);

    // Enregistrer le mouvement de stock
    MvtStk savedMvtStk = repository.save(MvtStkDto.toEntity(dto));

    // Retourner le DTO du mouvement de stock sauvegardé
    return MvtStkDto.fromEntity(savedMvtStk);
  }
}

/*@Service
@Slf4j
public class MvtStkServiceImpl implements MvtStkService {

  private MvtStkRepository repository;
  private ArticleService articleService;
  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public MvtStkServiceImpl(MvtStkRepository repository, ArticleService articleService, EntrepriseRepository entrepriseRepository) {
    this.repository = repository;
    this.articleService = articleService;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public BigDecimal stockReelArticle(Integer idArticle) {
    if (idArticle == null) {
      log.warn("ID article is NULL");
      return BigDecimal.valueOf(-1);
    }
    articleService.findById(idArticle);
    return repository.stockReelArticle(idArticle);
  }

  @Override
  public List<MvtStkDto> mvtStkArticle(Integer idArticle) {
    return repository.findAllByArticleId(idArticle).stream()
        .map(MvtStkDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public MvtStkDto entreeStock(MvtStkDto dto) {
    return entreePositive(dto, TypeMvtStk.ENTREE);
  }

  @Override
  public MvtStkDto sortieStock(MvtStkDto dto) {
    return sortieNegative(dto, TypeMvtStk.SORTIE);
  }

  @Override
  public MvtStkDto correctionStockPos(MvtStkDto dto) {
    return entreePositive(dto, TypeMvtStk.CORRECTION_POS);
  }

  @Override
  public MvtStkDto correctionStockNeg(MvtStkDto dto) {
    return sortieNegative(dto, TypeMvtStk.CORRECTION_NEG);
  }

  private MvtStkDto entreePositive(MvtStkDto dto, TypeMvtStk typeMvtStk) {
    List<String> errors = MvtStkValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Article is not valid {}", dto);
      throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STK_NOT_VALID, errors);
    }
    dto.setQuantite(
        BigDecimal.valueOf(
            Math.abs(dto.getQuantite().doubleValue())
        )
    );
    dto.setTypeMvt(typeMvtStk);
    return MvtStkDto.fromEntity(
        repository.save(MvtStkDto.toEntity(dto))
    );
  }

  private MvtStkDto sortieNegative(MvtStkDto dto, TypeMvtStk typeMvtStk) {
    List<String> errors = MvtStkValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Article is not valid {}", dto);
      throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STK_NOT_VALID, errors);
    }
    dto.setQuantite(
        BigDecimal.valueOf(
            Math.abs(dto.getQuantite().doubleValue()) * -1
        )
    );
    dto.setTypeMvt(typeMvtStk);
    return MvtStkDto.fromEntity(
        repository.save(MvtStkDto.toEntity(dto))
    );
  }
}*/
