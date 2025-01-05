package com.spring.teststock.servicesss;

import com.spring.teststock.dto.EntrepriseDto;

import java.util.List;

public interface EntrepriseService {

  EntrepriseDto save(EntrepriseDto dto);

  EntrepriseDto findById(Integer id);

  List<EntrepriseDto> findAll();

  void delete(Integer id);

}
