package com.spring.teststock.servicesss;

import com.spring.teststock.dto.MvtStkDto;
import com.spring.teststock.model.TypeMvtStk;

import java.math.BigDecimal;
import java.util.List;

public interface MvtStkService {

  BigDecimal stockReelArticle(Integer idArticle);

  List<MvtStkDto> mvtStkArticle(Integer idArticle);

  MvtStkDto entreeStock(MvtStkDto dto);

  MvtStkDto sortieStock(MvtStkDto dto);

  MvtStkDto correctionStockPos(MvtStkDto dto);

  MvtStkDto correctionStockNeg(MvtStkDto dto);

  // Ajouter la signature de la méthode générique handleStockMovement
  //MvtStkDto handleStockMovement(MvtStkDto dto, TypeMvtStk typeMvtStk);

}
