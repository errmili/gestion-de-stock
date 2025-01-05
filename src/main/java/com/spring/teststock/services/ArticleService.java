package com.spring.teststock.services;

import com.spring.teststock.dto.ArticleDto;
import com.spring.teststock.dto.LigneCommandeClientDto;
import com.spring.teststock.dto.LigneCommandeFournisseurDto;
import com.spring.teststock.dto.LigneVenteDto;

import java.util.List;

public interface ArticleService {

  ArticleDto save(ArticleDto dto);

  ArticleDto findById(Integer id);

  ArticleDto findByCodeArticle(String codeArticle);

  List<ArticleDto> findAll();

//  List<LigneVenteDto> findHistoriqueVentes(Integer idArticle);
//
//  List<LigneCommandeClientDto> findHistoriaueCommandeClient(Integer idArticle);
//
//  List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(Integer idArticle);
//
//  List<ArticleDto> findAllArticleByIdCategory(Integer idCategory);

  void delete(Integer id);

}
