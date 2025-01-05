package com.spring.teststock.dto;

import com.spring.teststock.model.Ventes;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
  public class VentesDto {

    private Integer id;

    private String code;

    private Instant dateVente;

    private String commentaire;

    private List<LigneVenteDto> ligneVentes;

    private Integer idEntreprise;

    public static VentesDto fromEntity(Ventes vente) {
      if (vente == null) {
        return null;
      }
      return VentesDto.builder()
          .id(vente.getId())
          .code(vente.getCode())
          .commentaire(vente.getCommentaire())
          .idEntreprise(vente.getIdEntreprise())
          .build();
    }

    public static Ventes toEntity(VentesDto dto) {
      if (dto == null) {
        return null;
      }
      Ventes ventes = new Ventes();
      ventes.setId(dto.getId());
      ventes.setCode(dto.getCode());
      ventes.setCommentaire(dto.getCommentaire());
      ventes.setIdEntreprise(dto.getIdEntreprise());
      return ventes;
    }

    /*
  // Création d'une vente depuis une commande client livrée
  public static Ventes creerVenteDepuisCommande(CommandeClient commandeClient) {
    if (commandeClient == null) {
      throw new IllegalArgumentException("La commande client ne peut pas être null.");
    }

    // Vérifie si la commande est livrée
    if (!commandeClient.isCommandeLivree()) {
      throw new IllegalStateException("La commande client n'est pas encore livrée.");
    }

    // Conversion de la commande client en vente
    Ventes ventes = new Ventes();
    ventes.setCode("VENTE-" + commandeClient.getCode()); // Génération d'un code basé sur la commande
    ventes.setDateVente(Instant.now()); // Date actuelle
    ventes.setCommentaire("Vente créée à partir de la commande client : " + commandeClient.getCode());
    ventes.setIdEntreprise(commandeClient.getIdEntreprise());
    ventes.setLigneVentes(commandeClient.getLigneCommandes() != null
            ? commandeClient.getLigneCommandes().stream()
            .map(LigneCommandeDto::toLigneVente) // Conversion des lignes
            .collect(Collectors.toList())
            : null);

    return ventes;
  }

     */
  }
