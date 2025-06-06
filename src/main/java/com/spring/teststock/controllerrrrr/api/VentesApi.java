package com.spring.teststock.controllerrrrr.api;

import com.spring.teststock.dto.VentesDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.spring.teststock.utils.Constants.VENTES_ENDPOINT;

@Api("ventes")
public interface VentesApi {

  @PostMapping(VENTES_ENDPOINT + "/create")
  VentesDto save(@RequestBody VentesDto dto);

  @GetMapping(VENTES_ENDPOINT + "/id/{idVente}")
  VentesDto findById(@PathVariable("idVente") Integer id);

  @GetMapping(VENTES_ENDPOINT + "/code/{codeVente}")
  VentesDto findByCode(@PathVariable("codeVente") String code);

  @GetMapping(VENTES_ENDPOINT + "/all")
  List<VentesDto> findAll();

  @DeleteMapping(VENTES_ENDPOINT + "/delete/{idVente}")
  void delete(@PathVariable("idVente") Integer id);

}
