package com.spring.teststock.controllerrrrr.api;

import io.swagger.annotations.Api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.spring.teststock.dto.auth.AuthenticationRequest;
import com.spring.teststock.dto.auth.AuthenticationResponse;

import static com.spring.teststock.utils.Constants.AUTHENTICATION_ENDPOINT;

@Api("authentication")
public interface AuthenticationApi {

  @PostMapping(AUTHENTICATION_ENDPOINT + "/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request);

}
