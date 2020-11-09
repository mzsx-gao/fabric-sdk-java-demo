package cn.com.fabric.sdk;

/**
 * 名称: Constants
 * 描述: 常量
 *
 * @author gaoshudian
 * @date 2020-11-09 17:48
 */
public class Constants {

    private static final String baseUrl = "/Users/gaoshudian/work/developer/workspace/personal/myworkspace/fabric-demo/src/main/resources/";

    //用户私钥和证书
    private static final String keyFolderPath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore";
    private static final String keyFileName = "e9163e7dfbd34e95f205a87e316ba9f69f5f50ff53d149edb81e0d98b13ad835_sk";
    private static final String certFoldePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts";
    private static final String certFileName = "Admin@org1.example.com-cert.pem";

    //创建channel所需的tx文件
    private static final String txfilePath = baseUrl + "channel.tx";

    //tls连接用的证书
    private static final String tlsOrderFilePath = baseUrl + "crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem";
    private static final String tlsPeerFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/tlscacerts/tlsca.org1.example.com-cert.pem";

    private static final String tlsPeerFilePathAddtion = baseUrl + "crypto-config/peerOrganizations/org2.example.com/tlsca/tlsca.org2.example.com-cert.pem";

    //chaincode路径
    private static final String chaincodePath = baseUrl + "chaincode";
}
