package mops.klausurzulassung.Token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokengenerierungTest {



  @Test
  public void testErstellenHashValue() {
    String matr = "3333333";
    String fach = "propra1";

    String ergebnis = Tokengenerierung.erstellenHashValue(matr, fach);

    assertEquals(ergebnis, "3333333propra1");
  }

  @Test
  public void testStudentenToken() {
    String matr = "3333333";
    String fach = "propra1";
    String token = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";

    String ergebnis = Tokengenerierung.erstellenQuittung(matr, fach, token);

    assertEquals(
        ergebnis, "3333333propra1a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
  }


}