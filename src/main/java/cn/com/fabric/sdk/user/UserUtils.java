package cn.com.fabric.sdk.user;

import org.hyperledger.fabric.sdk.Enrollment;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @description 用户工具类用于读取证书和私钥信息到java对象中
 */
public class UserUtils {

   private static class CAEnrollment implements Enrollment{
        private PrivateKey key;
        private String ecert;

        public CAEnrollment(PrivateKey key,String ecert) {
            this.key = key;
            this.ecert = ecert;
        }
        @Override
        public PrivateKey getKey() {
            return key;
        }

        @Override
        public String getCert() {
            return ecert;
        }
    }

    /**
     * @description 根据证书目录和私钥目录读取到enrollment里面。
     * @param keyFile 私钥文件
     * @param certFile 证书文件
     * @return enrollment 带有用户信息的对象
     */
    public static Enrollment getEnrollment(String keyFile,  String certFile) throws Exception {
        PrivateKey key;
        String certificate;
        BufferedReader brKey = null;
        try {
            brKey = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)));
            StringBuilder keyBuilder = new StringBuilder();

            for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
                if (line.indexOf("PRIVATE") == -1) {
                    keyBuilder.append(line);
                }
            }
            byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("ECDSA");
            key = kf.generatePrivate(keySpec);

            certificate = new String(Files.readAllBytes(Paths.get(certFile)));
        } finally {
            brKey.close();
        }
       return new CAEnrollment(key, certificate);
    }
}
