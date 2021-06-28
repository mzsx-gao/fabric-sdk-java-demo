package cn.com.fabric.sdk.constant;

/**
 * 名称: Constants
 * 描述: 常量
 *
 * @author gaoshudian
 * @date 2020-11-09 17:48
 */
public class Constants {

    //资源路径
    public static final String baseUrl = "/Users/gaoshudian/work/developer/workspace/personal/myworkspace/fabric-sdk-java-demo/src/main/resources/";

    //Admin用户私钥和证书
    public static final String KEY_FILE_PATH =  baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/e9163e7dfbd34e95f205a87e316ba9f69f5f50ff53d149edb81e0d98b13ad835_sk";
    public static final String CERT_FILE_PATH = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem";

    //创建channel所需的tx文件
    public static final String txfilePath = baseUrl + "channel.tx";

    //tls连接用的证书
    public static final String TLS_ORDER_FILE = baseUrl + "crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem";
    public static final String TLS_PEER_FILE =  baseUrl + "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/tlscacerts/tlsca.org1.example.com-cert.pem";

    //chaincode路径,根据下面两个目录找到chaincode,注意src路径是自动拼接上去的
    public static final String CHAINCODE_SOURCE_LOCATION = baseUrl + "chaincode";
    public static final String CHAINCODE_PATH = "basicInfo";
    //通道id
    public static final String CHANNEL_ID = "testchannel2";

    public static final String ORDER_NAME = "orderer.example.com";
    public static final String ORDER_GRPC_URL = "grpcs://orderer.example.com:7050";

    public static final String PEER_NAME = "peer0.org1.example.com";
    public static final String PEER_GRPC_URL = "grpcs://peer0.org1.example.com:7051";
}
