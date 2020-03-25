package mops.klausurzulassung.controller;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.Account;
import mops.klausurzulassung.domain.EmailError;
import mops.klausurzulassung.services.EmailErrorService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;

@Controller
@SessionScope
@RequestMapping("/zulassung1")
public class EmailErrorController {

  private EmailErrorService emailErrorService;

 @Autowired
  public EmailErrorController(EmailErrorService emailErrorService){
    this.emailErrorService = emailErrorService;
  }

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
            principal.getName(),
            principal.getKeycloakSecurityContext().getIdToken().getEmail(),
            null,
            token.getAccount().getRoles());
  }

  @Secured("ROLE_orga")
  @GetMapping("/emailError/{modulId}")
  public String emailErrorAnzeige(@PathVariable Long modulId, KeycloakAuthenticationToken token, Model model){
    ArrayList<EmailError> filteredAfterModulIdEmailErrors = filterAllEmailErrors(emailErrorService.getEmailErrors(), modulId);
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("emailErrors", filteredAfterModulIdEmailErrors);
    return "emailErrorPage";
  }

  private ArrayList<EmailError> filterAllEmailErrors(ArrayList<EmailError> allEmailErrors, Long modulId) {
    ArrayList<EmailError> filteredList = new ArrayList<>();
    for (EmailError emailError : allEmailErrors) {
        if(emailError.getStudent().getModulId().equals(modulId)){
          filteredList.add(emailError);
        }
    }
    return filteredList;
  }
}
