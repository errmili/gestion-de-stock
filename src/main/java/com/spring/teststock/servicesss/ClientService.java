package com.spring.teststock.servicesss;

import com.spring.teststock.dto.ClientDto;

import java.util.List;

public interface ClientService {

  // for test
  ClientDto save(ClientDto dto);

  ClientDto findById(Integer id);

  List<ClientDto> findAll();

  void delete(Integer id);

}
