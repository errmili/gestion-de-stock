package com.spring.teststock.servicesss.impl;

import com.spring.teststock.dto.*;
import com.spring.teststock.exception.EntityNotFoundException;
import com.spring.teststock.exception.ErrorCodes;
import com.spring.teststock.exception.InvalidEntityException;
import com.spring.teststock.exception.InvalidOperationException;
import com.spring.teststock.model.*;
import com.spring.teststock.repository.ArticleRepository;
import com.spring.teststock.repository.ClientRepository;
import com.spring.teststock.repository.CommandeClientRepository;
import com.spring.teststock.repository.EntrepriseRepository;
import com.spring.teststock.repository.LigneCommandeClientRepository;
import com.spring.teststock.servicesss.CommandeClientService;
import com.spring.teststock.servicesss.MvtStkService;
import com.spring.teststock.validator.ArticleValidator;
import com.spring.teststock.validator.CommandeClientValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommandeClientServiceImpl implements CommandeClientService {

  private CommandeClientRepository commandeClientRepository;
  private LigneCommandeClientRepository ligneCommandeClientRepository;
  private ClientRepository clientRepository;
  private ArticleRepository articleRepository;
  private MvtStkService mvtStkService;

  private EntrepriseRepository entrepriseRepository;

  @Autowired
  public CommandeClientServiceImpl(CommandeClientRepository commandeClientRepository,
                                   ClientRepository clientRepository, ArticleRepository articleRepository, LigneCommandeClientRepository ligneCommandeClientRepository,
                                   MvtStkService mvtStkService,
                                   EntrepriseRepository entrepriseRepository
                                   ) {
    this.commandeClientRepository = commandeClientRepository;
    this.ligneCommandeClientRepository = ligneCommandeClientRepository;
    this.clientRepository = clientRepository;
    this.articleRepository = articleRepository;
    this.mvtStkService = mvtStkService;
    this.entrepriseRepository = entrepriseRepository;
  }

  @Override
  public CommandeClientDto save(CommandeClientDto dto) {

    // Validation de la commande client
    List<String> errors = CommandeClientValidator.validate(dto);

    if (!errors.isEmpty()) {
      log.error("Commande client n'est pas valide");
      throw new InvalidEntityException("La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
    }

    // Vérification de l'entreprise référencée
    if (dto.getIdEntreprise() != null) {
      Optional<Entreprise> entrepriseOpt = entrepriseRepository.findById(dto.getIdEntreprise());
      if (!entrepriseOpt.isPresent()) {
        throw new InvalidEntityException("L'entreprise référencée n'existe pas.", ErrorCodes.ENTREPRISE_NOT_FOUND,
                Collections.singletonList("L'entreprise référencée dans la commande n'existe pas."));
      }
    } else {
      throw new InvalidEntityException("L'ID de l'entreprise est requis pour la commande", ErrorCodes.ENTREPRISE_NOT_FOUND,
              Collections.singletonList("L'ID de l'entreprise ne peut pas être nul pour la commande."));
    }

    if (dto.getId() != null && dto.isCommandeLivree()) {
      throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }

    // Vérification du client
    Optional<Client> client = clientRepository.findById(dto.getClient().getId());
    if (client.isEmpty()) {
      log.warn("Client with ID {} was not found in the DB", dto.getClient().getId());
      throw new EntityNotFoundException("Aucun client avec l'ID" + dto.getClient().getId() + " n'a ete trouve dans la BDD",
          ErrorCodes.CLIENT_NOT_FOUND);
    }

    // Vérification des articles dans les lignes de commande
    List<String> articleErrors = new ArrayList<>();

    if (dto.getLigneCommandeClients() != null) {
      dto.getLigneCommandeClients().forEach(ligCmdClt -> {
        if (ligCmdClt.getArticle() != null) {
          Optional<Article> article = articleRepository.findById(ligCmdClt.getArticle().getId());
          if (article.isEmpty()) {
            articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() + " n'existe pas");
          }
        } else {
          articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
        }
      });
    }

    if (!articleErrors.isEmpty()) {
      log.warn("");
      throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
    }

    // Initialiser la date de la commande
    dto.setDateCommande(Instant.now());
    CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));

    if (dto.getLigneCommandeClients() != null) {
      dto.getLigneCommandeClients().forEach(ligCmdClt -> {
        LigneCommandeClient ligneCommandeClient = LigneCommandeClientDto.toEntity(ligCmdClt);
        ligneCommandeClient.setCommandeClient(savedCmdClt);
        ligneCommandeClient.setIdEntreprise(dto.getIdEntreprise());
        LigneCommandeClient savedLigneCmd = ligneCommandeClientRepository.save(ligneCommandeClient);

        effectuerSortie(savedLigneCmd);
      });
    }

    return CommandeClientDto.fromEntity(savedCmdClt);
  }

  @Override
  public CommandeClientDto findById(Integer id) {
    checkIdCommande(id);
    return commandeClientRepository.findById(id)
        .map(CommandeClientDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
            "Aucune commande client n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
        ));
  }

  @Override
  public CommandeClientDto findByCode(String code) {
    if (!StringUtils.hasLength(code)) {
      log.error("Commande client CODE is NULL");
      return null;
    }
    return commandeClientRepository.findCommandeClientByCode(code)
        .map(CommandeClientDto::fromEntity)
        .orElseThrow(() -> new EntityNotFoundException(
            "Aucune commande client n'a ete trouve avec le CODE " + code, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
        ));
  }

  @Override
  public List<CommandeClientDto> findAll() {
    return commandeClientRepository.findAll().stream()
        .map(CommandeClientDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Integer id) {
    checkIdCommande(id);
    List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(id);
    if (!ligneCommandeClients.isEmpty()) {
      throw new InvalidOperationException("Impossible de supprimer une commande client deja utilisee",
          ErrorCodes.COMMANDE_CLIENT_ALREADY_IN_USE);
    }
    commandeClientRepository.deleteById(id);
  }

  @Override
  public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommande) {
    return ligneCommandeClientRepository.findAllByCommandeClientId(idCommande).stream()
        .map(LigneCommandeClientDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public CommandeClientDto updateEtatCommande(Integer idCommande, EtatCommande etatCommande) {
    checkIdCommande(idCommande);
    if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
      log.error("L'etat de la commande client is NULL");
      throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
    CommandeClientDto commandeClient = checkEtatCommande(idCommande);
    commandeClient.setEtatCommande(etatCommande);

    CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient));
    if (commandeClient.isCommandeLivree()) {
      updateMvtStk(idCommande);
      //Ventes vente = VentesDto.creerVenteDepuisCommande(commandeClient);
      //ventesRepository.save(vente);
    }

    return CommandeClientDto.fromEntity(savedCmdClt);
  }

  @Override
  public CommandeClientDto updateQuantiteCommande(Integer idCommande, Integer idLigneCommande, BigDecimal quantite) {
    checkIdCommande(idCommande);
    checkIdLigneCommande(idLigneCommande);

    if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
      log.error("L'ID de la ligne commande is NULL");
      throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }

    CommandeClientDto commandeClient = checkEtatCommande(idCommande);
    Optional<LigneCommandeClient> ligneCommandeClientOptional = findLigneCommandeClient(idLigneCommande);

    LigneCommandeClient ligneCommandeClient = ligneCommandeClientOptional.get();
    ligneCommandeClient.setQuantite(quantite);
    ligneCommandeClientRepository.save(ligneCommandeClient);

    return commandeClient;
  }

  @Override
  public CommandeClientDto updateClient(Integer idCommande, Integer idClient) {
    checkIdCommande(idCommande);
    if (idClient == null) {
      log.error("L'ID du client is NULL");
      throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID client null",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
    CommandeClientDto commandeClient = checkEtatCommande(idCommande);
    Optional<Client> clientOptional = clientRepository.findById(idClient);
    if (clientOptional.isEmpty()) {
      throw new EntityNotFoundException(
          "Aucun client n'a ete trouve avec l'ID " + idClient, ErrorCodes.CLIENT_NOT_FOUND);
    }
    commandeClient.setClient(ClientDto.fromEntity(clientOptional.get()));

    return CommandeClientDto.fromEntity(
        commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient))
    );
  }

  @Override
  public CommandeClientDto updateArticle(Integer idCommande, Integer idLigneCommande, Integer idArticle) {
    checkIdCommande(idCommande);
    checkIdLigneCommande(idLigneCommande);
    checkIdArticle(idArticle, "nouvel");

    CommandeClientDto commandeClient = checkEtatCommande(idCommande);

    Optional<LigneCommandeClient> ligneCommandeClient = findLigneCommandeClient(idLigneCommande);

    Optional<Article> articleOptional = articleRepository.findById(idArticle);
    if (articleOptional.isEmpty()) {
      throw new EntityNotFoundException(
          "Aucune article n'a ete trouve avec l'ID " + idArticle, ErrorCodes.ARTICLE_NOT_FOUND);
    }

    List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(articleOptional.get()));
    if (!errors.isEmpty()) {
      throw new InvalidEntityException("Article invalid", ErrorCodes.ARTICLE_NOT_VALID, errors);
    }

    LigneCommandeClient ligneCommandeClientToSaved = ligneCommandeClient.get();
    ligneCommandeClientToSaved.setArticle(articleOptional.get());
    ligneCommandeClientRepository.save(ligneCommandeClientToSaved);

    return commandeClient;
  }

  @Override
  public CommandeClientDto deleteArticle(Integer idCommande, Integer idLigneCommande) {
    checkIdCommande(idCommande);
    checkIdLigneCommande(idLigneCommande);

    CommandeClientDto commandeClient = checkEtatCommande(idCommande);
    // Just to check the LigneCommandeClient and inform the client in case it is absent
    findLigneCommandeClient(idLigneCommande);
    ligneCommandeClientRepository.deleteById(idLigneCommande);

    return commandeClient;
  }

  private CommandeClientDto checkEtatCommande(Integer idCommande) {
    CommandeClientDto commandeClient = findById(idCommande);
    if (commandeClient.isCommandeLivree()) {
      throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
    return commandeClient;
  }

  private Optional<LigneCommandeClient> findLigneCommandeClient(Integer idLigneCommande) {
    Optional<LigneCommandeClient> ligneCommandeClientOptional = ligneCommandeClientRepository.findById(idLigneCommande);
    if (ligneCommandeClientOptional.isEmpty()) {
      throw new EntityNotFoundException(
          "Aucune ligne commande client n'a ete trouve avec l'ID " + idLigneCommande, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND);
    }
    return ligneCommandeClientOptional;
  }

  private void checkIdCommande(Integer idCommande) {
    if (idCommande == null) {
      log.error("Commande client ID is NULL");
      throw new InvalidOperationException("COMMANDE_CLIENT_ID_NULL",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
  }

  private void checkIdLigneCommande(Integer idLigneCommande) {
    if (idLigneCommande == null) {
      log.error("L'ID de la ligne commande is NULL");
      throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une ligne de commande null",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
  }

  private void checkIdArticle(Integer idArticle, String msg) {
    if (idArticle == null) {
      log.error("L'ID de " + msg + " is NULL");
      throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
          ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
    }
  }

  private void updateMvtStk(Integer idCommande) {
    List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(idCommande);
    ligneCommandeClients.forEach(lig -> {
      effectuerSortie(lig);
    });
  }

  private void effectuerSortie(LigneCommandeClient lig) {
    MvtStkDto mvtStkDto = MvtStkDto.builder()
        .article(ArticleDto.fromEntity(lig.getArticle()))
        .dateMvt(Instant.now())
        .typeMvt(TypeMvtStk.SORTIE)
        .sourceMvt(SourceMvtStk.COMMANDE_CLIENT)
        .quantite(lig.getQuantite())
        .idEntreprise(lig.getIdEntreprise())
        .build();
    mvtStkService.sortieStock(mvtStkDto);
  }

}
