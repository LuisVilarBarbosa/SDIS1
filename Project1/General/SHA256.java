package Project1.General;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {

    public static final int BYTES = 32; // Bytes = 256bits / 8

    public static String SHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(data.getBytes());
        return DatatypeConverter.printHexBinary(hash);
    }
}
