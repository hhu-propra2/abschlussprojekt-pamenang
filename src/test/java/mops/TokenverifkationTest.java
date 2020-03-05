package mops;

import mops.klausurzulassung.Token.Tokenverifikation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class TokenverifkationTest {

    @Test
    public void testverifikation(){
        String token="de8944d29e5f01ad1f6f9f0203bcbc11e2809c72706b43d4bbdc77d7d4c32288";
        String matr ="2770736";
        String fachid="1111111";

        assertTrue(Tokenverifikation.verifikation(matr,fachid,token));
    }
}