package com.spring.teststock.controllerrrrr.api;

import com.spring.teststock.dto.EntrepriseDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.spring.teststock.utils.Constants.ENTREPRISE_ENDPOINT;

@Api("entreprises")
public interface EntrepriseApi {

  @PostMapping(ENTREPRISE_ENDPOINT + "/create")
  EntrepriseDto save(@RequestBody EntrepriseDto dto);

  @GetMapping(ENTREPRISE_ENDPOINT + "/{idEntreprise}")
  EntrepriseDto findById(@PathVariable("idEntreprise") Integer id);

  @GetMapping(ENTREPRISE_ENDPOINT + "/all")
  List<EntrepriseDto> findAll();

  @DeleteMapping(ENTREPRISE_ENDPOINT + "/delete/{idEntreprise}")
  void delete(@PathVariable("idEntreprise") Integer id);

}
