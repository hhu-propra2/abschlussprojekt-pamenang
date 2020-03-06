package mops.klausurzulassung.Services.Token;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

@Service
public class TokenverifikationService {

    //WICHTIG!!!
    //Key muss noch angepasst werden! s. Zeile 13
    public boolean verifikationToken(String matr, String fachID, String token) throws NoSuchAlgorithmException, SignatureException {

        String HashValue = matr+fachID;


        Signature sign = Signature.getInstance("SHA256withRSA");
        //sign.initVerify(/*publicKey*/);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        byte[] tokenByte = hexStringToByteArray(token);
        return sign.verify(tokenByte);
    }

    byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
