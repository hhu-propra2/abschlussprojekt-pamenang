package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.QuittungDao;
import mops.klausurzulassung.Domain.QuittungDto;
import mops.klausurzulassung.Services.QuittungService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;

@Service
public class TokengenerierungService {

    private Logger logger = LoggerFactory.getLogger(TokengenerierungService.class);
    private final QuittungService quittungService;

    @Autowired
    public TokengenerierungService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public String erstellenHashValue(String matr, String fach){
        return matr+fach;
    }

    public String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public String erstellenToken(String matr, String fachID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        String HashValue = erstellenHashValue(matr, fachID);
        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privateKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        logger.debug("Sign SHA256 with RSA");

        sign.initSign(privateKey);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        PublicKey publicKey = pair.getPublic();
        byte[] token = sign.sign();


        QuittungDto quittungDto = new QuittungDto(matr, fachID, publicKey, Base64.getEncoder().encodeToString(token));
        QuittungDao quittungDao = erstelleQuittungDao(quittungDto);

        quittungService.save(quittungDao);
        logger.debug("Speichere Quittung von  Student: "+quittungDao.getMatrikelnummer()+ " in Datenbank");

        String s = Base64.getEncoder().encodeToString(token);
        System.out.println(s);
        return s;
    }

    private KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(512);
        logger.debug("Generiere KeyPaar");
        return keyPairGen.generateKeyPair();
    }

    private QuittungDao erstelleQuittungDao(QuittungDto quittungDto){
        QuittungDao quittungDao = new QuittungDao();
        quittungDao.setFachID(quittungDto.getFachID());
        quittungDao.setMatrikelnummer(quittungDto.getMatrikelnummer());
        quittungDao.setPublicKey(quittungDto.getPublicKey());
        quittungDao.setToken(quittungDto.getToken());
        return quittungDao;

    }
}
