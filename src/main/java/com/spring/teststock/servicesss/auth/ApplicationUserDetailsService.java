package com.spring.teststock.servicesss.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.teststock.dto.UtilisateurDto;
import com.spring.teststock.model.auth.ExtendedUser;
import com.spring.teststock.servicesss.UtilisateurService;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

  @Autowired
  private UtilisateurService service;


  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UtilisateurDto utilisateur = service.findByEmail(email);

    // Vérification si l'utilisateur est null
    if (utilisateur == null) {
      throw new UsernameNotFoundException("Utilisateur non trouvé pour l'email : " + email);
    }

    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

    if (utilisateur.getRoles() != null) {
      utilisateur.getRoles().forEach(role ->
              authorities.add(new SimpleGrantedAuthority(role.getRoleName()))
      );
    }

    // Retourner un ExtendedUser avec les informations nécessaires
    return new ExtendedUser(
            utilisateur.getEmail(),
            utilisateur.getMoteDePasse(),
            utilisateur.getEntreprise().getId(),
            authorities
    );
  }
}
