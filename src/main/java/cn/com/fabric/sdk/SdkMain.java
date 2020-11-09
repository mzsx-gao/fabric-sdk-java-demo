package cn.com.fabric.sdk;

import org.hyperledger.fabric.sdk.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * fabric-sdk-java测试
 */
public class SdkMain {

    //资源路径
    private static final String baseUrl = "/Users/gaoshudian/work/developer/workspace/personal/myworkspace/fabric-demo/src/main/resources/";

    //用户私钥和证书
    private static final String keyFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/e9163e7dfbd34e95f205a87e316ba9f69f5f50ff53d149edb81e0d98b13ad835_sk";
    private static final String certFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem";

    //创建channel所需的tx文件
    private static final String txfilePath = baseUrl + "channel.tx";

    //tls连接用的证书
    private static final String tlsOrderFilePath = baseUrl + "crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem";
    private static final String tlsPeerFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/tlscacerts/tlsca.org1.example.com-cert.pem";

    private static final String tlsPeerFilePathAddtion = baseUrl + "crypto-config/peerOrganizations/org2.example.com/tlsca/tlsca.org2.example.com-cert.pem";

    //chaincode路径
    private static final String chaincodePath = baseUrl + "chaincode";

    public FabricClient getFabricClient() throws Exception {
        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org1");
        userContext.setMspId("Org1MSP");
        userContext.setAccount("Admin");
        userContext.setName("Admin");
        Enrollment enrollment = UserUtils.getEnrollment(keyFilePath, certFilePath);
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        return fabricClient;
    }

    //创建channel
    @Test
    public void createChannel() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", tlsOrderFilePath);
        Channel channel = fabricClient.createChannel("testchannel", orderer, txfilePath);
        System.out.println(channel);
    }

    //获取已有的channel,将peer节点加入应用通道
    @Test
    public void joinPeer() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", tlsOrderFilePath);
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", tlsPeerFilePath);
        Channel channel = fabricClient.getChannel("testchannel");
        channel.addOrderer(orderer);
        channel.joinPeer(peer);
        channel.initialize();
        System.out.println(channel);
    }

    //安装合约
    @Test
    public void chaincodeInstall() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Peer peer0 = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", tlsPeerFilePath);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer0);
        fabricClient.installChaincode(TransactionRequest.Type.GO_LANG, "basicinfo", "1.0", chaincodePath, "basicinfo", peers);
    }

    //合约实例化
    @Test
    public void chaincodeInstantiation() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", tlsPeerFilePath);
        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", tlsOrderFilePath);
        String initArgs[] = {""};
        fabricClient.initChaincode("testchannel", TransactionRequest.Type.GO_LANG, "basicinfo", "1.0", order, peer, "init", initArgs);
    }

    //合约升级
    @Test
    public void chaincodeUpgrade() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com","grpcs://peer0.org1.example.com:7051",tlsPeerFilePath);
        Orderer order = fabricClient.getOrderer("orderer.example.com","grpcs://orderer.example.com:7050",tlsOrderFilePath);
        String initArgs[] = {""};
        fabricClient.upgradeChaincode("mychannel", TransactionRequest.Type.GO_LANG,"basicinfo","2.0",order,peer,"init",initArgs);
    }

    //调用合约
    @Test
    public void chaincodeInvoke() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", tlsPeerFilePath);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", tlsOrderFilePath);
        String initArgs[] = {"110114","{\"name\":\"zhangsan\",\"identity\":\"110114\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke("testchannel", TransactionRequest.Type.GO_LANG,"basicinfo",order,peers,"save",initArgs);
    }

    //查询合约
    @Test
    public void chaincodeQuery() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", tlsPeerFilePath);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        String initArgs[] = {"110114"};
        Map map =  fabricClient.queryChaincode(peers,"testchannel", TransactionRequest.Type.GO_LANG,"basicinfo","query",initArgs);
        System.out.println(map);
    }

    //注册用户 hqCZUStrRTAR
   /*public static void main(String[] args) throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://192.168.70.43",null);
        UserContext register = new UserContext();
        register.setName("lihua");
        register.setAffiliation("org2");
        Enrollment enrollment = caClient.enroll("admin","adminpw");
        UserContext registar = new UserContext();
        registar.setName("admin");
        registar.setAffiliation("org2");
        registar.setEnrollment(enrollment);
       String secret =  caClient.register(registar,register);
       System.out.println(secret);
    }*/

    //注册用户查询合约
//    public static void main(String[] args) throws Exception {
//        FabricCAClient caClient = new FabricCAClient("http://192.168.70.43",null);
//        UserContext userContext = new UserContext();
//        userContext.setAffiliation("Org2");
//        userContext.setMspId("Org2MSP");
//        userContext.setAccount("李伟");
//        userContext.setName("admin");
//        Enrollment enrollment = caClient.enroll("lihua","hqCZUStrRTAR");
//        userContext.setEnrollment(enrollment);
//        FabricClient fabricClient = new FabricClient(userContext);
//        Peer peer0 = fabricClient.getPeer("peer0.org1.example.com","grpcs://peer0.org1.example.com:7051",tlsPeerFilePath);
//        Peer peer1 = fabricClient.getPeer("peer0.org2.example.com","grpcs://peer0.org2.example.com:9051",tlsPeerFilePathAddtion);
//        List<Peer> peers = new ArrayList<>();
//        peers.add(peer0);
//        peers.add(peer1);
//        String initArgs[] = {"110120"};
//        Map map =  fabricClient.queryChaincode(peers,"mychannel", TransactionRequest.Type.GO_LANG,"basicinfo","query",initArgs);
//        System.out.println(map);
//    }


    //注册用户invoke合约
    /*public static void main(String[] args) throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://192.168.70.43",null);
        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org2");
        userContext.setMspId("Org2MSP");
        userContext.setAccount("李伟");
        userContext.setName("admin");
        Enrollment enrollment = caClient.enroll("lihua","hqCZUStrRTAR");
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        Peer peer0 = fabricClient.getPeer("peer0.org1.example.com","grpcs://peer0.org1.example.com:7051",tlsPeerFilePath);
        Peer peer1 = fabricClient.getPeer("peer0.org2.example.com","grpcs://peer0.org2.example.com:9051",tlsPeerFilePathAddtion);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer0);
        peers.add(peer1);
        Orderer order = fabricClient.getOrderer("orderer.example.com","grpcs://orderer.example.com:7050",tlsOrderFilePath);
        String initArgs[] = {"110120","{\"name\":\"zhangsan\",\"identity\":\"110120\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke("mychannel", TransactionRequest.Type.GO_LANG,"basicinfo",order,peers,"save",initArgs);
    }*/
}