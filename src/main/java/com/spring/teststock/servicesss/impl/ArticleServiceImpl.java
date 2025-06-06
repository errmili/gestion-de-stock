package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.ArticleDto;
import com.spring.teststock.dto.LigneCommandeClientDto;
import com.spring.teststock.dto.LigneCommandeFournisseurDto;
import com.spring.teststock.dto.LigneVenteDto;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.exception.InvalidOperationException;
import com.spring.teststock.model.Category;
import com.spring.teststock.model.Entreprise;
import com.spring.teststock.model.LigneCommandeClient;
import com.spring.teststock.model.LigneCommandeFournisseur;
import com.spring.teststock.model.LigneVente;
import com.spring.teststock.repository.ArticleRepository;
import com.spring.teststock.repository.CategoryRepository;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.LigneCommandeClientRepository;
import com.spring.teststock.repository.LigneCommandeFournisseurRepository;
import com.spring.teststock.repository.LigneVenteRepository;
import com.spring.teststock.servicesss.ArticleService;
import com.spring.teststock.validator.ArticleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

  private ArticleRepository articleRepository;

  private EntrepriseRepository entrepriseRepository;

  private CategoryRepository categoryRepository;
//  private LigneVenteRepository venteRepository;
//  private LigneCommandeFournisseurRepository commandeFournisseurRepository;
//  private LigneCommandeClientRepository commandeClientRepository;

//  @Autowired
//  public ArticleServiceImpl(
//      ArticleRepository articleRepository,
//      LigneVenteRepository venteRepository, LigneCommandeFournisseurRepository commandeFournisseurRepository,
//      LigneCommandeClientRepository commandeClientRepository) {
//    this.articleRepository = articleRepository;
//    this.venteRepository = venteRepository;
//    this.commandeFournisseurRepository = commandeFournisseurRepository;
//    this.commandeClientRepository = commandeClientRepository;
//  }

  @Autowired
  public ArticleServiceImpl(
          CategoryRepository categoryRepository,
          EntrepriseRepository entrepriseRepository,
          ArticleRepository articleRepository) {
    this.categoryRepository = categoryRepository;
    this.entrepriseRepository = entrepriseRepository;
    this.articleRepository = articleRepository;
  }

  @Override
  public ArticleDto save(ArticleDto dto) {
    List<String> errors = ArticleValidator.validate(dto);
    if (!errors.isEmpty()) {
      log.error("Article is not valid {}", dto);
      throw new InvalidEntityException("L'article n'est pas valide", ErrorCodes.ARTICLE_NOT_VALID, errors);
    }

    // Vérifier si l'entreprise existe
    if (dto.getIdEntreprise() != null) {
      Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getIdEntreprise());
      if (!entrepriseOpt.isPresent()) {
        throw new InvalidEntityException("L'entreprise référencée n'existe pas.", ErrorCodes.ENTREPRISE_NOT_FOUND,
                Collections.singletonList("L'entreprise référencée dans l'article n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de l'entreprise est requis pour l'article", ErrorCodes.ENTREPRISE_NOT_FOUND,
              Collections.singletonList("L'ID de l'entreprise ne peut pas être nul pour l'article."));
    }

    // Vérifier si la catégorie existe
    if (dto.getCategory() != null && dto.getCategory().getId() != null) {
      Optional<Category> categoryOpt = categoryRepository.findById(dto.getCategory().getId());
      if (!categoryOpt.isPresent()) {
        throw new InvalidEntityException("La catégorie référencée n'existe pas.", ErrorCodes.CATEGORY_NOT_FOUND,
                Collections.singletonList("La catégorie référencée dans l'article n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de la catégorie est requis pour l'article", ErrorCodes.CATEGORY_NOT_FOUND,
              Collections.singletonList("L'ID de la catégorie ne peut pas être nul pour l'article."));
    }


    return ArticleDto.fromEntity(
        articleRepository.save(
            ArticleDto.toEntity(dto)
        )
    );
  }

  @Override
  public ArticleDto findById(Integer id) {
    if (id == null) {
      log.error("Article ID is null");
      return null;
    }

    return articleRepository.findById(id).map(ArticleDto::fromEntity).orElseThrow(() ->
        new EntityNotFoundException(
            "Aucun article avec l'ID = " + id + " n' ete trouve dans la BDD",
            ErrorCodes.ARTICLE_NOT_FOUND)
    );
  }

  @Override
  public ArticleDto findByCodeArticle(String codeArticle) {
    if (!StringUtils.hasLength(codeArticle)) {
      log.error("Article CODE is null");
      return null;
    }

    return articleRepository.findArticleByCodeArticle(codeArticle)
        .map(ArticleDto::fromEntity)
        .orElseThrow(() ->
            new EntityNotFoundException(
                "Aucun article avec le CODE = " + codeArticle + " n' ete trouve dans la BDD",
                ErrorCodes.ARTICLE_NOT_FOUND)
        );
  }

  @Override
  public List<ArticleDto> findAll() {
    return articleRepository.findAll().stream()
        .map(ArticleDto::fromEntity)
        .collect(Collectors.toList());
  }

//  @Override
//  public List<LigneVenteDto> findHistoriqueVentes(Integer idArticle) {
//    return venteRepository.findAllByArticleId(idArticle).stream()
//        .map(LigneVenteDto::fromEntity)
//        .collect(Collectors.toList());
//  }

//  @Override
//  public List<LigneCommandeClientDto> findHistoriaueCommandeClient(Integer idArticle) {
//    return commandeClientRepository.findAllByArticleId(idArticle).stream()
//        .map(LigneCommandeClientDto::fromEntity)
//        .collect(Collectors.toList());
//  }

//  @Override
//  public List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(Integer idArticle) {
//    return commandeFournisseurRepository.findAllByArticleId(idArticle).stream()
//        .map(LigneCommandeFournisseurDto::fromEntity)
//        .collect(Collectors.toList());
//  }

  @Override
  public List<ArticleDto> findAllArticleByIdCategory(Integer idCategory) {
    return articleRepository.findAllByCategoryId(idCategory).stream()
        .map(ArticleDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    if (id == null) {
      log.error("Article ID is null");
      return;
    }
//    List<LigneCommandeClient> ligneCommandeClients = commandeClientRepository.findAllByArticleId(id);
//    if (!ligneCommandeClients.isEmpty()) {
//      throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes client", ErrorCodes.ARTICLE_ALREADY_IN_USE);
//    }
//    List<LigneCommandeFournisseur> ligneCommandeFournisseurs = commandeFournisseurRepository.findAllByArticleId(id);
//    if (!ligneCommandeFournisseurs.isEmpty()) {
//      throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes fournisseur",
//          ErrorCodes.ARTICLE_ALREADY_IN_USE);
//    }
//    List<LigneVente> ligneVentes = venteRepository.findAllByArticleId(id);
//    if (!ligneVentes.isEmpty()) {
//      throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des ventes",
//          ErrorCodes.ARTICLE_ALREADY_IN_USE);
//    }
    articleRepository.deleteById(id);
  }
}
