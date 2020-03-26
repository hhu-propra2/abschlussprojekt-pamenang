package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.QuittungDao;
import mops.klausurzulassung.domain.QuittungDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
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

    public String erstellenHashValue(String matr, String modulID) {
        return matr + modulID;
    }

    public String erstellenToken(String matr, String modulID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        String hashValue = erstellenHashValue(matr, modulID);
        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privateKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");

        sign.initSign(privateKey);
        byte[] hashValueBytes = hashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        PublicKey publicKey = pair.getPublic();
        byte[] token = sign.sign();

        String base64Token = Base64.getEncoder().encodeToString(token);
        String base64Matr = Base64.getEncoder().encodeToString(matr.getBytes());
        String base64ModulID = Base64.getEncoder().encodeToString(modulID.getBytes());

        String quittung = base64Token + "§" + base64Matr + "§" + base64ModulID;

        // Slash wird ersetzt, da sonst Fehler bei Linkgenierierung auftreten
        quittung = quittung.replaceAll("/", "@");

        logger.debug("Quittung wurde erstellt und ist encoded:" + quittung);


        QuittungDto quittungDto = new QuittungDto(matr, modulID, publicKey, quittung);
        QuittungDao quittungDao = erstelleQuittungDao(quittungDto);

        quittungService.save(quittungDao);
        logger.debug("Speichere Quittung von  Student: " + quittungDao.getMatrikelnummer() + " in Datenbank");

        return quittung;
    }

    private KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(512);
        logger.debug("Generiere KeyPaar");
        return keyPairGen.generateKeyPair();
    }

    private QuittungDao erstelleQuittungDao(QuittungDto quittungDto) {
        QuittungDao quittungDao = new QuittungDao();
        quittungDao.setModulId(quittungDto.getModulId());
        quittungDao.setMatrikelnummer(quittungDto.getMatrikelnummer());
        quittungDao.setPublicKey(quittungDto.getPublicKey());
        quittungDao.setQuittung(quittungDto.getQuittung());
        return quittungDao;

    }
}
