package mops.klausurzulassung.controller;

import mops.klausurzulassung.domain.Account;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.repositories.StudentRepository;
import mops.klausurzulassung.services.StudentService;
import mops.klausurzulassung.services.TokenverifikationService;
import mops.klausurzulassung.wrapper.Token;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StudentenController {

  @Autowired
  TokenverifikationService tokenverifikation;
  @Autowired
  StudentRepository studentRepository;
  @Autowired
  StudentService studentService;
  private FrontendMessage message = new FrontendMessage();
  private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured({"ROLE_studentin", "ROLE_orga"})
  @GetMapping("/student/{tokenLink}/")
  public String studentansichtMitToken(@PathVariable String tokenLink, Model model, KeycloakAuthenticationToken keyToken) {
    model.addAttribute("account", createAccountFromPrincipal(keyToken));
    model.addAttribute("token", new Token(tokenLink));
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();
    return "student";
  }

  @GetMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("student", false);
    model.addAttribute("token", new Token(""));

    if (token.getAccount().getPrincipal().toString().equals("studentin")) {
      model.addAttribute("student", true);
      logger.debug(token.getAccount().getPrincipal().toString());
    }

    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();


    return "student";
  }

  @PostMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String empfangeDaten(@ModelAttribute("token") Token token, KeycloakAuthenticationToken keycloakAuthenticationToken, Model model) {

    logger.debug("Token: " + token.getToken());

    long[] verifizierungsErgebnis;
    try {
      verifizierungsErgebnis = tokenverifikation.verifikationToken(token.getToken());
    } catch (Exception e) {
      logger.error("Die Tokenverifikation ist fehlgeschlagen!");
      logger.error(e.getMessage());
      message.setErrorMessage("Token nicht valide!");
      return "redirect:/zulassung1/student";
    }

    logger.debug("ModulID: " + verifizierungsErgebnis[0]);
    logger.debug("Matrikelnummer : " + verifizierungsErgebnis[1]);
      Student student = Student.builder()
          .matrikelnummer(verifizierungsErgebnis[0])
          .modulId(verifizierungsErgebnis[1]).build();
      studentService.save(student);
      message.setSuccessMessage("Altzulassung erfolgreich!");
      logger.debug("Altzulassung erfolgreich!");

    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("student", false);
    if (keycloakAuthenticationToken.getAccount().getPrincipal().toString().equals("studentin"))
      model.addAttribute("student", true);
    model.addAttribute("token", "");
    return "redirect:/zulassung1/student";
  }
}